package com.app.Service;

import com.app.Model.Follower;
import com.app.Model.User;
import java.util.List;

public interface FollowerService {
  List<User> getAllFollowingByUserId(long userId, int limit);

  Follower followUser(long followerId);

  void unfollowUser(long followerId);
}
