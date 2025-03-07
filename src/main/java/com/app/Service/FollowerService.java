package com.app.Service;

import com.app.Model.Follower;

import java.util.List;

public interface FollowerService {
    List<Follower> getAllFollowingByUserId(long userId,int limit, int offset);
    Follower addFollowerToUser(long followerId);
    void removeFollowerFromUser(long followerId);
}
