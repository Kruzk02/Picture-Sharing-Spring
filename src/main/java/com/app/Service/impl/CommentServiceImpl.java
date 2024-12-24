package com.app.Service.impl;

import com.app.DAO.CommentDao;
import com.app.DAO.MediaDao;
import com.app.DAO.PinDao;
import com.app.DAO.UserDao;
import com.app.DTO.request.CreateCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.Model.*;
import com.app.Service.CommentService;
import com.app.exception.sub.CommentIsEmptyException;
import com.app.exception.sub.CommentNotFoundException;
import com.app.exception.sub.PinNotFoundException;
import com.app.exception.sub.UserNotMatchException;
import com.app.utils.FileUtils;
import com.app.utils.MediaUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log4j2
@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentDao commentDao;
    private final UserDao userDao;
    private final PinDao pinDao;
    private final MediaDao mediaDao;
    private final MediaUtils mediaUtils;
    private final FileUtils fileUtils;
    private final RedisTemplate<Object,Object> redisTemplate;

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userDao.findUserByUsername(authentication.getName());
    }

    @Override
    public Comment save(CreateCommentRequest request) {
        if ((request.content() == null || request.content().trim().isEmpty()) && request.media().isEmpty()) {
            throw new CommentIsEmptyException("A comment must have either content or a media attachment.");
        }
        String filename = mediaUtils.generateUniqueFilename(request.media().getOriginalFilename());
        String extension = mediaUtils.getFileExtension(request.media().getOriginalFilename());

        fileUtils.save(request.media(), filename, extension);
        Media media = mediaDao.save(Media.builder()
                .url(filename)
                .mediaType(MediaType.fromExtension(extension))
                .build());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Comment comment = Comment.builder()
                .content(request.content())
                .pinId(request.pinId())
                .mediaId(media.getId())
                .userId(userDao.findUserByUsername(authentication.getName()).getId())
                .build();
        return commentDao.save(comment);
    }

    @Override
    public Comment update(Long id, UpdatedCommentRequest request) {
        Comment comment = commentDao.findDetailsById(id);
        if (comment == null) {
            throw new CommentNotFoundException("Comment not found with a id: " + id);
        }

        if (!Objects.equals(getAuthenticatedUser().getId(), comment.getUserId())) {
            throw new UserNotMatchException("User not matching with a comment");
        }

        if ((request.content() == null || request.content().trim().isEmpty()) && (request.media().isEmpty() || request.media().isEmpty())) {
            throw new CommentIsEmptyException("A comment must have either content or a media attachment.");
        }

        if (request.media() != null && !request.media().isEmpty()) {
            Media existingMedia = mediaDao.findByCommentId(comment.getId());
            String extensionOfExistingMedia = mediaUtils.getFileExtension(existingMedia.getUrl());

            String filename = mediaUtils.generateUniqueFilename(request.media().getOriginalFilename());
            String extension = mediaUtils.getFileExtension(request.media().getOriginalFilename());

            CompletableFuture.runAsync(() -> fileUtils.delete(existingMedia.getUrl(), extensionOfExistingMedia))
                    .thenRunAsync(() -> fileUtils.save(request.media(), filename, extension));

            mediaDao.update(comment.getMediaId(), Media.builder()
                    .url(filename)
                    .mediaType(MediaType.fromExtension(extension))
                    .build());
        }

        if (request.content() != null && !request.content().trim().isEmpty()) {
            comment.setContent(request.content());
        }

        return commentDao.update(id, comment);
    }

    @Override
    public Comment findBasicById(Long id) {
        Comment cacheComment = (Comment) redisTemplate.opsForValue().get("comment_basic: " + id);
        if (cacheComment != null) {
            log.info("Cache hit for basic comment with a id: {}", id);
            return cacheComment;
        }
        log.info("Cache miss for basic comment with a id: {}", id);
        Comment comment = commentDao.findBasicById(id);
        if (comment != null) {
            redisTemplate.opsForValue().set("comment_basic: " + id, comment, Duration.ofHours(1));
        }
        return comment;
    }

    @Override
    public Comment findDetailsById(Long id) {
        Comment cacheCOmment = (Comment) redisTemplate.opsForValue().get("comment_detail: " + id);
        if (cacheCOmment != null) {
            log.info("Cache hit for detail comment with a id: {}", id);
            return cacheCOmment;
        }
        log.info("Cache miss for detail comment with a id: {}", id);
        Comment comment = commentDao.findDetailsById(id);
        if (comment != null) {
            redisTemplate.opsForValue().set("comment_detail: " + id, comment, Duration.ofHours(1));
        }
        return comment;
    }

    @Override
    public List<Comment> findByPinId(Long pinId, int limit, int offset) {
        List<Comment> comments = commentDao.findByPinId(pinId, limit, offset);
        List<Object> cacheKeys = comments.stream()
                .map(comment -> "comments: " + comment.getId())
                .collect(Collectors.toList());
        List<Object> cacheComments = redisTemplate.opsForValue().multiGet(cacheKeys);

        for (int i = 0; i < Objects.requireNonNull(cacheComments).size(); i++) {
            if (cacheComments.get(i) == null) {
                log.info("Cache hit for comment with a id: {}", comments.get(i).getId());
                redisTemplate.opsForValue().set("comments: " + comments.get(i).getId(), comments.get(i), Duration.ofHours(1));
            }
        }
        return comments;
    }

    @Override
    public List<Comment> findNewestByPinId(Long pinId, int limit, int offset) {
        List<Comment> comments = commentDao.findNewestByPinId(pinId, limit, offset);
        List<Object> cacheKEys = comments.stream()
                .map(comment -> "comments_newest: " + comment.getId())
                .collect(Collectors.toList());
        List<Object> cacheComments = redisTemplate.opsForValue().multiGet(cacheKEys);

        for (int i = 0;i < Objects.requireNonNull(cacheComments).size();i++) {
            if (cacheComments.get(i) == null) {
                log.info("Cache hit for newest comment with a id: {}", comments.get(i).getId());
                redisTemplate.opsForValue().set("comments_newest: " + comments.get(i).getId(), comments.get(i), Duration.ofHours(1));
            }
        }
        return comments;
    }

    @Override
    public List<Comment> findOldestByPinId(Long pinId, int limit, int offset) {
        List<Comment> comments = commentDao.findOldestByPinId(pinId, limit, offset);
        List<Object> cacheKey = comments.stream()
                .map(comment -> "comments_oldest: " + comment.getId())
                .collect(Collectors.toList());
        List<Object> cacheComments = redisTemplate.opsForValue().multiGet(cacheKey);

        for (int i = 0;i < Objects.requireNonNull(cacheComments).size();i++) {
            if (cacheComments.get(i) == null) {
                log.info("Cache hit for oldest comment with a id: {}", comments.get(i).getId());
                redisTemplate.opsForValue().set("comment_oldest: " + comments.get(i).getId(), comments.get(i), Duration.ofHours(1));
            }
        }
        return comments;
    }

    @Override
    public void deleteById(Long id) {
        Comment comment = commentDao.findBasicById(id);
        if(comment != null && Objects.equals(getAuthenticatedUser().getId(),comment.getUserId())){
            commentDao.deleteById(comment.getId());
            redisTemplate.delete("comment_basic: " + id);
            redisTemplate.delete("comment_detail: " + id);
        } else {
            throw new UserNotMatchException("User does not match with a comment");
        }
    }

    @Override
    public void deleteByPinId(Long pinId) {
        Pin pin = pinDao.findById(pinId);
        if (pin != null && Objects.equals(pin.getUserId(), getAuthenticatedUser().getId())) {
            commentDao.deleteByPinId(pinId);
        } else if (pin == null) {
            throw new PinNotFoundException("Pin not found with a id: " + pin);
        } else {
            throw new UserNotMatchException("User does not match with a pin");
        }
    }
}
