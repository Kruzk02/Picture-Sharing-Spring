package com.app.DAO;

import com.app.Model.Follower;
import com.app.Model.User;

import java.util.List;

public interface FollowerDao {
    List<User> getAllFollowingByUserId(long userId, int limit);
    boolean isFollowing(long followerId, long userId);
    Follower followUser(long followerId, long userId);
    int unfollowUser(long followerId, long userId);
}
