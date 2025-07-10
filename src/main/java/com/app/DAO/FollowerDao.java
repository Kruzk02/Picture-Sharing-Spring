package com.app.DAO;

import com.app.Model.Follower;
import com.app.Model.User;

import java.util.List;

public interface FollowerDao {
    List<User> getAllFollowingByUserId(long userId, int limit);
    boolean isFollowing(long authUserId, long targetId);
    Follower followUser(long authUserId, long targetId);
    int unfollowUser(long authUserId, long targetId);
}
