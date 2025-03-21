package com.app.Service.impl;

import com.app.DAO.FollowerDao;
import com.app.DAO.UserDao;
import com.app.Model.Follower;
import com.app.Model.User;
import com.app.Service.FollowerService;
import com.app.exception.sub.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Service
public class FollowerServiceImpl implements FollowerService {

    private final FollowerDao followerDao;
    private final UserDao userDao;

    private User getAuthenticationUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userDao.findUserByUsername(authentication.getName());
    }

    @Override
    public List<User> getAllFollowingByUserId(long userId,int limit) {
        List<User> users = followerDao.getAllFollowingByUserId(userId, limit);
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        return users;
    }

    @Override
    public Follower addFollowerToUser(long followerId) {
        User follower = userDao.findUserById(followerId);
        if (follower == null) {
            throw new UserNotFoundException("User not found with a id: " + followerId);
        }

        long authUserId = getAuthenticationUser().getId();

        if (Objects.equals(authUserId, follower.getId())) {
            throw new IllegalArgumentException("Users cannot follow themselves.");
        }

        if (followerDao.isUserAlreadyFollowing(follower.getId(), authUserId)) {
            throw new RuntimeException("User is already following.");
        }

        return followerDao.addFollowerToUser(follower.getId(), authUserId);
    }

    @Override
    public void removeFollowerFromUser(long followerId) {
        User follower = userDao.findUserById(followerId);
        if (follower == null) {
            throw new UserNotFoundException("User not found with ID: " + followerId);
        }

        long authUserId = getAuthenticationUser().getId();

        if (!followerDao.isUserAlreadyFollowing(follower.getId(), authUserId)) {
            throw new RuntimeException("User is not following.");
        }

        followerDao.removeFollowerFromUser(follower.getId(), authUserId);
    }
}
