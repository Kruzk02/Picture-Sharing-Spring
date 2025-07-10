package com.app.Service.impl;

import com.app.DAO.FollowerDao;
import com.app.DAO.UserDao;
import com.app.Model.Follower;
import com.app.Model.Gender;
import com.app.Model.User;
import com.app.exception.sub.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FollowerServiceImplTest {

    @Mock private FollowerDao followerDao;
    @Mock private UserDao userDao;

    @InjectMocks private FollowerServiceImpl followerService;

    private User user1;
    private User user2;
    private Follower follower;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .username("username")
                .email("email@gmail.com")
                .password("encodedPassword")
                .gender(Gender.OTHER)
                .build();
        user2 = User.builder()
                        .id(2L)
                        .username("username2")
                        .email("email1@gmail.com")
                        .password("encodedPassword")
                        .gender(Gender.OTHER)
                        .build();

        follower = Follower.builder()
                .followingId(1L)
                .followerId(2L)
                .build();
    }

    @Test
    void getAllFollowingByUserId_shouldReturnListOfUser() {
        Mockito.when(followerDao.getAllFollowingByUserId(2L, 10)).thenReturn(List.of(user1));

        var result = followerService.getAllFollowingByUserId(2L, 10);
        assertEquals(List.of(user1), result);
    }

    @Test
    void getAllFollowingByUserId_shouldReturnEmptyList() {
        Mockito.when(followerDao.getAllFollowingByUserId(1L, 10)).thenReturn(Collections.emptyList());

        List<User> result = followerService.getAllFollowingByUserId(1L, 10);

        assertTrue(result.isEmpty());
    }

    private void mockAuthentication(User user) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(authentication.getName()).thenReturn(user.getUsername());
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(userDao.findUserByUsername(user.getUsername())).thenReturn(user);
    }

    @Test
    void followUser_success() {
        mockAuthentication(user1);

        Mockito.when(userDao.findUserById(2L)).thenReturn(user2);
        Mockito.when(followerDao.isFollowing(1L, 2L)).thenReturn(false);

        Mockito.when(followerDao.followUser(1L, 2L)).thenReturn(follower);

        Follower result = followerService.followUser(2L);

        assertEquals(follower, result);
    }

    @Test
    void followUser_userNotFound_shouldThrow() {
        Mockito.when(userDao.findUserById(99L)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> {
            followerService.followUser(99L);
        });
    }

    @Test
    void unfollowUser_success() {
        mockAuthentication(user1);

        Mockito.when(userDao.findUserById(2L)).thenReturn(user2);
        Mockito.when(followerDao.isFollowing( 1L, 2L)).thenReturn(true);

        assertDoesNotThrow(() -> followerService.unfollowUser(2L));
        Mockito.verify(followerDao).unfollowUser(1L, 2L);
    }

    @Test
    void unfollowUser_userNotFound_shouldThrow() {
        Mockito.when(userDao.findUserById(99L)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> {
            followerService.unfollowUser(99L);
        });
    }
}