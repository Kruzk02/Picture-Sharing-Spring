package com.app.DAO.Impl;

import com.app.DAO.FollowerDao;
import com.app.Model.Follower;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@AllArgsConstructor
@Repository
public class FollowerDaoImpl implements FollowerDao {

    private final JdbcTemplate template;

    @Override
    public List<Follower> getAllFollowingByUserId(long userId, int limit, int offset) {
        try {
            String sql = "SELECT follower_id, following_id FROM followers WHERE follower_id = ? LIMIT ? OFFSET ?";
            return template.query(sql,(rs, rowNum) ->
                    Follower.builder()
                            .followerId(rs.getLong("follower_id"))
                            .followingId(rs.getLong("following_id"))
                            .build()
            , userId, limit, offset);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error fetching followers: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isUserAlreadyFollowing(long followerId, long userId) {
        try {
            String sql = "SELECT COUNT(*) FROM followers WHERE follower_id = ? AND following_id = ?";
            Integer count = template.queryForObject(sql, Integer.class, followerId, userId);
            return count > 0;
        } catch (DataAccessException e) {
            throw new RuntimeException("Error checking follow status: " + e.getMessage(), e);
        }
    }

    @Override
    public Follower addFollowerToUser(long followerId, long userId) {
        try {
            String sql = "INSERT INTO followers (follower_id, following_id) VALUES (?, ?)";
            int rowsAffected = template.update(sql, followerId, userId);
            if (rowsAffected > 0) {
                return Follower.builder()
                        .followerId(followerId)
                        .followingId(userId)
                        .build();
            }
            return null;
        } catch (DataAccessException e) {
            throw new RuntimeException("Error adding follower: " + e.getMessage(), e);
        }
    }

    @Override
    public int removeFollowerFromUser(long followerId, long userId) {
        try {
            String sql = "DELETE FROM followers WHERE follower_id = ? AND following_id = ?";
            return template.update(sql, followerId, userId);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error removing follower: " + e.getMessage(), e);
        }
    }
}
