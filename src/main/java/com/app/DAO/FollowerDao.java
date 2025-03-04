package com.app.DAO;

import com.app.Model.Follower;

import java.util.List;

public interface FollowerDao {
    List<Follower> getAllFollowersByUserId(long userId, int limit, int offset);
    Follower addFollowerToUser(long followerId, long userId);
    int removeFollowerFromUser(long followerId, long userId);
}
