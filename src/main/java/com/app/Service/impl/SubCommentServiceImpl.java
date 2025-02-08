package com.app.Service.impl;

import com.app.DAO.CommentDao;
import com.app.DAO.MediaDao;
import com.app.DAO.SubCommentDao;
import com.app.DAO.UserDao;
import com.app.DTO.request.CreateSubCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.Model.*;
import com.app.Service.SubCommentService;
import com.app.exception.sub.CommentIsEmptyException;
import com.app.exception.sub.SubCommentNotFoundException;
import com.app.exception.sub.UserNotMatchException;
import com.app.utils.FileUtils;
import com.app.utils.MediaUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class SubCommentServiceImpl implements SubCommentService {

    private final SubCommentDao subCommentDao;
    private final CommentDao commentDao;
    private final UserDao userDao;
    private final MediaDao mediaDao;
    private final MediaUtils mediaUtils;
    private final FileUtils fileUtils;
    private final RedisTemplate<String,SubComment> subCommentRedisTemplate;

    @Override
    public SubComment save(CreateSubCommentRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String filename = mediaUtils.generateUniqueFilename(request.file().getOriginalFilename());
        String extension = mediaUtils.getFileExtension(request.file().getOriginalFilename());

        fileUtils.save(request.file(), filename, extension);
        Media media = mediaDao.save(Media.builder()
                .url(filename)
                .mediaType(MediaType.fromExtension(extension))
                .build());

        SubComment subComment = SubComment.builder()
                .content(request.content())
                .comment(commentDao.findBasicById(request.commentId()))
                .user(userDao.findUserByUsername(authentication.getName()))
                .media(media)
                .createAt(Timestamp.from(Instant.now()))
                .build();

        SubComment savedSubComment = subCommentDao.save(subComment);
        subCommentRedisTemplate.opsForValue().set("subComment:" + savedSubComment.getId(),savedSubComment, Duration.ofHours(2));

        return savedSubComment;
    }

    @Override
    public SubComment update(long id, UpdatedCommentRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDao.findUserByUsername(authentication.getName());

        SubComment subComment = subCommentDao.findById(id);
        if (subComment == null) {
            throw new SubCommentNotFoundException("Sub comment not found with a id: " + id);
        }

        if (!Objects.equals(user.getId(), subComment.getUser().getId())) {
            throw new UserNotMatchException("User does not match with sub comment");
        }

        if ((request.content() == null || request.content().trim().isEmpty()) && (request.media().isEmpty() || request.media().isEmpty())) {
            throw new CommentIsEmptyException("A comment must have either content or a media attachment.");
        }

        subCommentRedisTemplate.delete("subComment:" + id);

        if (request.media() != null && !request.media().isEmpty()) {
            String extensionOfMedia = mediaUtils.getFileExtension(subComment.getMedia().getUrl());

            String filename = mediaUtils.generateUniqueFilename(request.media().getOriginalFilename());
            String extension = mediaUtils.getFileExtension(request.media().getOriginalFilename());

            CompletableFuture.runAsync(() -> fileUtils.delete(subComment.getMedia().getUrl(), extensionOfMedia)
                    .thenRunAsync(() -> fileUtils.save(request.media(), filename, extension)));

            mediaDao.update(subComment.getMedia().getId(), Media.builder()
                    .url(filename)
                    .mediaType(MediaType.fromExtension(extension))
                    .build());
        }

        if (request.content() != null && !request.content().trim().isEmpty()) {
            subComment.setContent(request.content());
        }

        subCommentRedisTemplate.opsForValue().set("subComment:" + id, subComment, Duration.ofHours(2));
        return subCommentDao.update(id, subComment);
    }

    @Override
    public SubComment findById(long id) {
        String cacheKey = "subComment:" + id;

        SubComment cacheSubComment = subCommentRedisTemplate.opsForValue().get(cacheKey);
        if (cacheSubComment != null) {
            return cacheSubComment;
        }

        SubComment subComment = subCommentDao.findById(id);
        if (subComment == null) {
            throw new SubCommentNotFoundException("Sub comment not found with a id: " + id);
        }

        subCommentRedisTemplate.opsForValue().set(cacheKey,subComment,Duration.ofHours(2));
        return subComment;
    }

    @Override
    public List<SubComment> findAllByCommentId(long commentId, int limit, int offset) {
        String redisKey = "comment:" + commentId + ":subComments";
        
        List<SubComment> cachedSubComment = subCommentRedisTemplate.opsForList().range(redisKey, offset, offset + limit - 1);
        if (cachedSubComment != null && !cachedSubComment.isEmpty()) {
            return cachedSubComment;
        }

        List<SubComment> subComments = subCommentDao.findAllByCommentId(commentId, limit, offset);
        if (subComments.isEmpty()) {
            return Collections.emptyList();
        }

        subCommentRedisTemplate.opsForList().rightPushAll(redisKey, subComments);
        subCommentRedisTemplate.expire(redisKey, Duration.ofHours(2));

        return subComments;
    }

    @Override
    public List<SubComment> findNewestByCommentId(long commentId, int limit, int offset) {
        String redisKey = "comment:" + commentId + ":subComments:newest";

        List<SubComment> cachedSubComment = subCommentRedisTemplate.opsForList().range(redisKey, offset, offset + limit - 1);
        if (cachedSubComment != null && !cachedSubComment.isEmpty()) {
            return cachedSubComment;
        }

        List<SubComment> subComments = subCommentDao.findNewestByCommentId(commentId, limit, offset);
        if (subComments.isEmpty()) {
            return Collections.emptyList();
        }

        subCommentRedisTemplate.opsForList().rightPushAll(redisKey, subComments);
        subCommentRedisTemplate.expire(redisKey, Duration.ofHours(2));

        return subComments;
    }

    @Override
    public List<SubComment> findOldestByCommentId(long commentId, int limit, int offset) {
        String redisKey = "comment:" + commentId + ":subComments";

        List<SubComment> cachedSubComment = subCommentRedisTemplate.opsForList().range(redisKey, offset, offset + limit - 1);
        if (cachedSubComment != null && !cachedSubComment.isEmpty()) {
            return cachedSubComment;
        }

        List<SubComment> subComments = subCommentDao.findOldestByCommentId(commentId, limit, offset);
        if (subComments.isEmpty()) {
            return Collections.emptyList();
        }

        subCommentRedisTemplate.opsForList().rightPushAll(redisKey, subComments);
        subCommentRedisTemplate.expire(redisKey, Duration.ofHours(2));

        return subComments;
    }

    @Override
    public void deleteById(long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDao.findUserByUsername(authentication.getName());

        SubComment subComment = subCommentDao.findById(id);
        if (subComment == null) {
            throw new SubCommentNotFoundException("Sub comment not found with id: " + id);
        }

        if (!Objects.equals(subComment.getUser().getId(), user.getId())) {
            throw new UserNotMatchException("Authenticated user does not own the sub comment");
        }

        System.out.println(subComment.getComment().getId());
        subCommentRedisTemplate.delete("subComment:" + id);
        subCommentDao.deleteById(id);
    }
}
