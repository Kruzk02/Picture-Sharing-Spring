package com.app.Service;

import com.app.DAO.RoleDao;
import com.app.DAO.UserDao;
import com.app.DTO.LoginDTO;
import com.app.DTO.request.LoginUserRequest;
import com.app.DTO.request.RegisterUserRequest;
import com.app.DTO.request.UpdateUserRequest;
import com.app.Model.Gender;
import com.app.Model.User;
import com.app.exception.sub.FileNotSupportException;
import com.app.exception.sub.UserAlreadyExistsException;
import com.app.exception.sub.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * User service class responsible for user related operations such as registration, login, and retrieval.<p>
 * This class interacts with the UserDaoImpl for data access and utilizes ModelMapper for mapping between DTOs and entity object.
 */
@Service
@AllArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user based on the provided registerDTO.<p>
     * Maps the RegisterDTO to a User entity. encodes the password, and saves the userDao.
     *
     * @param request The RegisterRequestDTO object containing user registration information.
     * @return The registered User entity.
     */
    public User register(RegisterUserRequest request) throws IOException {

        if (userDao.findUserByEmail(request.email()) != null) {
            throw new UserAlreadyExistsException("Email is already taken.");
        }

        if (userDao.findUserByUsername(request.username()) != null) {
            throw new UserAlreadyExistsException("Username is already taken.");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .gender(Gender.OTHER)
                .profilePicture(getDefaultProfilePicturePath())
                .roles(Arrays.asList(roleDao.findByName("ROLE_USER")))
                .build();

        return userDao.register(user);
    }

    /**
     * Authenticates a user based on provided loginDTO.<p>
     * Use the authenticationManager to authenticate the user and sets the authentication in the SecurityContextHolder.
     *
     * @param request The LoginUserRequest object containing user login credentials.
     * @return The authentication User entity.
     */
    public User login(LoginUserRequest request){
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return userDao.login(request.username());
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    /**
     * Retrieves a user by username.
     *
     * @param username The username of the user to retrieve.
     * @return The User entity corresponding to the provided username.
     */
    public User findUserByUsername(String username){
        return userDao.findUserByUsername(username);
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id The ID of the user to retrieve.
     * @return The User entity corresponding to the provided ID.
     */
    public User findUserById(Long id){
        return userDao.findUserById(id);
    }

    /**
     * Retrieves a user by email.
     *
     * @param email The email of the user to retrieve.
     * @return The User entity corresponding to the provided email.
     */
    public User findUserByEmail(String email){
        return userDao.findUserByEmail(email);
    }

    public User update(UpdateUserRequest request, String username, MultipartFile profilePicture) throws IOException {
        User user = userDao.findFullUserByUsername(username);

        if (user == null) {
            throw new UserNotFoundException("User not found with a username: " + username);
        }

        if (request.email() != null && !request.email().equals(user.getEmail()) && userDao.findUserByEmail(request.email()) != null) {
            throw new UserAlreadyExistsException("Email is already taken.");
        }

        if (request.username() != null && !request.username().equals(user.getUsername()) && userDao.findUserByUsername(request.username()) != null) {
            throw new UserAlreadyExistsException("Username is already taken.");
        }

        user.setUsername(request.username() != null ? request.username() : user.getUsername());
        user.setEmail(request.email() != null ? request.email() : user.getEmail());
        user.setPassword(request.password() != null ? passwordEncoder.encode(request.password()) : user.getPassword());
        user.setBio(request.bio() != null ? request.bio() : user.getBio());
        user.setGender(request.gender() != null ? request.gender() : user.getGender());

        if (profilePicture != null && !profilePicture.isEmpty()) {
            saveProfilePicture(user, profilePicture);
        } else {
            user.setProfilePicture(user.getProfilePicture());
        }

        return userDao.update(user);
    }

    private void saveProfilePicture(User user, MultipartFile profilePicture) throws IOException {
        String originFilename = profilePicture.getOriginalFilename();
        if (originFilename != null && originFilename.contains(".")) {
            String fileExtension = originFilename.substring(originFilename.lastIndexOf("."));

            if (!fileExtension.matches(".png|.jpg|.jpeg")) {
                throw new FileNotSupportException("File type not support: " + fileExtension);
            }

            String fileCode = RandomStringUtils.randomAlphabetic(8) + fileExtension;
            Path path = Paths.get("profile_picture");

            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            Path filePath = path.resolve(fileCode);

            try (InputStream inputStream = profilePicture.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                user.setProfilePicture(fileCode);
            } catch (IOException e) {
                user.setProfilePicture(getDefaultProfilePicturePath());
            }
        }
    }

    private String getDefaultProfilePicturePath() {
        Resource defaultProfilePic = new ClassPathResource("static/default_profile_picture.png");
        if (defaultProfilePic.exists()) {
            return "static/default_profile_picture.png";
        } else {
            System.out.println("Warning: Default profile picture not found.");
            return null;
        }
    }
}
