package com.app.DAO;

import com.app.Model.Follower;

import java.util.List;

public interface FollowerDao {
    List<Follower> getAllFollowingByUserId(long userId, int limit, int offset);
    boolean isUserAlreadyFollowing(long followerId, long userId);
    Follower addFollowerToUser(long followerId, long userId);
    int removeFollowerFromUser(long followerId, long userId);
}
