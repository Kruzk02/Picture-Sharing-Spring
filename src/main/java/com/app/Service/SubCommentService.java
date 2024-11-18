package com.app.Service;

import com.app.DAO.CommentDao;
import com.app.DAO.SubCommentDao;
import com.app.DAO.UserDao;
import com.app.DTO.request.CreateSubCommentRequest;
import com.app.Model.SubComment;
import com.app.Model.User;
import com.app.exception.sub.UserNotMatchException;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SubCommentService {

    private final SubCommentDao subCommentDao;
    private final CommentDao commentDao;
    private final UserDao userDao;
    private final RedisTemplate<Object,Object> redisTemplate;

    public SubComment save(CreateSubCommentRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        SubComment subComment = SubComment.builder()
                .content(request.content())
                .comment(commentDao.findById(request.commentId()))
                .user(userDao.findUserByUsername(authentication.getName()))
                .timestamp(Timestamp.from(Instant.now()))
                .build();

        SubComment savedSubComment = subCommentDao.save(subComment);
        redisTemplate.opsForValue().set("subComment:" + savedSubComment.getId(),savedSubComment, Duration.ofHours(2));

        return savedSubComment;
    }

    public List<SubComment> findAllByCommentId(Long commentId) {
        List<SubComment> subComments = subCommentDao.findAllByCommentId(commentId);
        List<Object> cacheKeys = subComments.stream()
                .map(subComment -> "subComment:" + subComment.getId())
                .collect(Collectors.toList());

        List<Object> cacheSubComment = redisTemplate.opsForValue().multiGet(cacheKeys);

        for (int i = 0;i < Objects.requireNonNull(cacheSubComment).size();i++) {
            if (cacheSubComment.get(i) == null) {
                redisTemplate.opsForValue().set(cacheKeys.get(i),subComments.get(i),Duration.ofHours(2));
            }
        }
        return subComments;
    }

    public SubComment findById(Long id) {
        SubComment cacheSubComment = (SubComment) redisTemplate.opsForValue().get("subComment:" + id);
        if (cacheSubComment != null) {
            return cacheSubComment;
        }

        SubComment subComment = subCommentDao.findById(id);
        if (subComment != null) {
            redisTemplate.opsForValue().set("subComment" + id,subComment,Duration.ofHours(2));
        }
        return subComment;
    }

    public void deleteIfUserMatches(User user,Long id) {
        SubComment subComment = subCommentDao.findById(id);
        if (subComment != null && Objects.equals(user.getId(),subComment.getUser().getId())) {
            subCommentDao.deleteById(subComment.getId());
        } else {
            throw new UserNotMatchException("User does not match with sub-comment owner");
        }
    }
}
