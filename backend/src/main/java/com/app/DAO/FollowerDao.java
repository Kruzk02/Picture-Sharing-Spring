package com.app.DAO;

import com.app.Model.Follower;
import com.app.Model.User;

import java.util.List;

public interface FollowerDao {
    List<User> getAllFollowingByUserId(long userId, int limit);
    boolean isUserAlreadyFollowing(long followerId, long userId);
    Follower addFollowerToUser(long followerId, long userId);
    int removeFollowerFromUser(long followerId, long userId);
}
