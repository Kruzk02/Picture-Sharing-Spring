package com.app.Service;

import com.app.DAO.Impl.UserDaoImpl;
import com.app.DTO.LoginDTO;
import com.app.DTO.RegisterDTO;
import com.app.Model.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * User service class responsible for user related operations such as registration, login, and retrieval.<p>
 * This class interacts with the UserDaoImpl for data access and utilizes ModelMapper for mapping between DTOs and entity object.
 */
@Service
public class UserService {

    private final UserDaoImpl userDao;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Construct a new UserService.
     *
     * @param userDao The UserDaoImpl for accessing user-related data.
     * @param modelMapper The ModelMapper for entity-DTO mapping.
     * @param passwordEncoder The PasswordEncoder for encoding user passwords.
     * @param authenticationManager The AuthenticationManager for user authentication.
     */
    @Autowired
    public UserService(UserDaoImpl userDao, ModelMapper modelMapper, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userDao = userDao;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user based on the provided registerDTO.<p>
     * Maps the RegisterDTO to a User entity. encodes the password, and saves the userDao.
     *
     * @param registerDTO The RegisterDTO object containing user registration information.
     * @return The registered User entity.
     */
    public User register(RegisterDTO registerDTO){
        User user = modelMapper.map(registerDTO,User.class);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        return userDao.register(user);
    }

    /**
     * Authenticates a user based on provided loginDTO.<p>
     * Use the authenticationManager to authenticate the user and sets the authentication in the SecurityContextHolder.
     *
     * @param loginDTO The LoginDTO object containing user login credentials.
     * @return The authentication User entity.
     */
    public User login(LoginDTO loginDTO){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return userDao.login(loginDTO.getUsername());
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
}
