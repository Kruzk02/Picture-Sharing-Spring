package com.app.Service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.app.DAO.MediaDao;
import com.app.DAO.RoleDao;
import com.app.DAO.UserDao;
import com.app.DTO.request.LoginUserRequest;
import com.app.DTO.request.RegisterUserRequest;
import com.app.DTO.request.UpdateUserRequest;
import com.app.Model.*;
import com.app.Service.VerificationTokenService;
import com.app.exception.sub.UserAlreadyExistsException;
import com.app.message.producer.EmailEventProducer;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock private UserDao userDao;
  @Mock private RoleDao roleDao;
  @Mock private MediaDao mediaDao;
  @Mock private VerificationTokenService verificationTokenService;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private EmailEventProducer emailEventProducer;

  @InjectMocks UserServiceImpl userService;

  private RegisterUserRequest registerUserRequest;
  private LoginUserRequest loginUserRequest;
  private UpdateUserRequest updateUserRequest;

  private User user;
  private Role role;

  @BeforeEach
  void setUp() {
    registerUserRequest = new RegisterUserRequest("username", "email@gmail.com", "password");
    loginUserRequest = new LoginUserRequest("username", "password");
    updateUserRequest =
        new UpdateUserRequest(
            "newUsername", "new@example.com", "newPassword", "Updated bio", Gender.FEMALE, null);

    Media media =
        Media.builder()
            .id(1L)
            .mediaType(MediaType.IMAGE)
            .url("default_profile_picture.png")
            .build();
    role = Role.builder().id(1L).name("ROLE_USER").build();

    user =
        User.builder()
            .username(registerUserRequest.username())
            .email(registerUserRequest.email())
            .password("encodedPassword")
            .roles(List.of(role))
            .media(media)
            .gender(Gender.OTHER)
            .build();
  }

  @Test
  void register_shouldThrowExceptionIfEmailExists() {
    Mockito.when(userDao.findUserByEmail(registerUserRequest.email())).thenReturn(new User());
    assertThrows(UserAlreadyExistsException.class, () -> userService.register(registerUserRequest));
  }

  @Test
  void register_shouldThrowExceptionIfUsernameExists() {
    Mockito.when(userDao.findUserByEmail(registerUserRequest.email())).thenReturn(null);
    Mockito.when(userDao.findUserByUsername(registerUserRequest.username())).thenReturn(new User());
    assertThrows(UserAlreadyExistsException.class, () -> userService.register(registerUserRequest));
  }

  @Test
  void register_shouldRegisterUserSuccessfully() {
    Mockito.when(userDao.findUserByEmail(registerUserRequest.email())).thenReturn(null);
    Mockito.when(userDao.findUserByUsername(registerUserRequest.username())).thenReturn(null);
    Mockito.when(passwordEncoder.encode(registerUserRequest.password()))
        .thenReturn("encodedPassword");

    Mockito.when(roleDao.findByName("ROLE_USER")).thenReturn(role);

    Mockito.when(
            userDao.register(
                Mockito.argThat(
                    u ->
                        u.getUsername().equals("username")
                            && u.getEmail().equals("email@gmail.com"))))
        .thenReturn(user);

    var token =
        VerificationToken.builder()
            .token("token")
            .userId(user.getId())
            .expireDate(Date.valueOf(LocalDate.now()))
            .build();

    Mockito.when(verificationTokenService.generateVerificationToken(Mockito.eq(user)))
        .thenReturn(token);

    var result = userService.register(registerUserRequest);

    assertNotNull(result);
    assertEquals("username", result.getUsername());
    assertEquals("email@gmail.com", result.getEmail());

    Mockito.verify(emailEventProducer).send(Mockito.any(VerificationEmailEvent.class));
  }

  @Test
  void login_shouldReturnUser_whenCredentialsAreValid() {
    var token =
        new UsernamePasswordAuthenticationToken(
            loginUserRequest.username(), loginUserRequest.password());

    var authentication = mock(Authentication.class);

    Mockito.when(authenticationManager.authenticate(token)).thenReturn(authentication);
    Mockito.when(userDao.login("username")).thenReturn(user);

    var result = userService.login(loginUserRequest);

    assertNotNull(result);
    assertEquals(user.getUsername(), result.getUsername());
  }

  @Test
  void login_shouldThrowBadCredentialsException_whenAuthenticationFails() {
    Mockito.when(authenticationManager.authenticate(Mockito.any()))
        .thenThrow(new BadCredentialsException("Bad credentials"));
    assertThrows(BadCredentialsException.class, () -> userService.login(loginUserRequest));
  }

  @Test
  void update_shouldUpdateUserSuccessfully() {
    Authentication auth = Mockito.mock(Authentication.class);
    Mockito.when(auth.getName()).thenReturn("username");

    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(securityContext);

    Mockito.when(userDao.findFullUserByUsername("username")).thenReturn(user);
    Mockito.when(userDao.findUserByEmail("new@example.com")).thenReturn(null);
    Mockito.when(userDao.findUserByUsername("newUsername")).thenReturn(null);
    Mockito.when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
    Mockito.when(
            userDao.update(
                Mockito.argThat(
                    user ->
                        user.getUsername().equals("newUsername")
                            && user.getEmail().equals("new@example.com")
                            && user.getPassword().equals("encodedNewPassword")
                            && user.getBio().equals("Updated bio")
                            && user.getGender() == Gender.FEMALE)))
        .thenAnswer(i -> i.getArgument(0));

    var updatedUser = userService.update(updateUserRequest);

    assertNotNull(updatedUser);
    assertEquals("newUsername", updatedUser.getUsername());
    assertEquals("new@example.com", updatedUser.getEmail());
    assertEquals("encodedNewPassword", updatedUser.getPassword());
    assertEquals("Updated bio", updatedUser.getBio());
    assertEquals(Gender.FEMALE, updatedUser.getGender());

    Mockito.verify(userDao)
        .update(
            Mockito.argThat(
                user ->
                    user.getUsername().equals("newUsername")
                        && user.getEmail().equals("new@example.com")
                        && user.getPassword().equals("encodedNewPassword")
                        && user.getBio().equals("Updated bio")
                        && user.getGender() == Gender.FEMALE));
  }

  @Test
  void findFullUserByUsername_shouldReturnUser() {
    Mockito.when(userDao.findFullUserByUsername("username")).thenReturn(user);

    var result = userService.findFullUserByUsername("username");

    assertNotNull(result);
    assertEquals("username", result.getUsername());
    assertEquals("email@gmail.com", result.getEmail());

    Mockito.verify(userDao).findFullUserByUsername("username");
  }

  @Test
  void verifyAccount_shouldCallVerificationService() {
    String token = "test-verification-token";

    userService.verifyAccount(token);

    Mockito.verify(verificationTokenService, Mockito.times(1)).verifyAccount(token);
  }
}
