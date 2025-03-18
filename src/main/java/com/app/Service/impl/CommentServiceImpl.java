package com.app.Service.impl;

import com.app.DAO.CommentDao;
import com.app.DAO.MediaDao;
import com.app.DAO.UserDao;
import com.app.DTO.request.CreateCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.Model.*;
import com.app.Service.CommentService;
import com.app.exception.sub.CommentIsEmptyException;
import com.app.exception.sub.CommentNotFoundException;
import com.app.exception.sub.UserNotMatchException;
import com.app.utils.FileUtils;
import com.app.utils.MediaUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentDao commentDao;
    private final UserDao userDao;
    private final MediaDao mediaDao;
    private final MediaUtils mediaUtils;
    private final FileUtils fileUtils;
    private final RedisTemplate<String, Comment> commentRedisTemplate;
    private final Map<Long, SseEmitter> emitters;

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
        Comment savedComment = commentDao.save(comment);

        SseEmitter emitter = emitters.get(request.pinId());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("new-comment")
                        .data(savedComment));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(request.pinId());
            }
        }

        return savedComment;
    }

    @Override
    public Comment update(Long id, UpdatedCommentRequest request) {
        Comment comment = commentDao.findById(id, true);
        if (comment == null) {
            throw new CommentNotFoundException("Comment not found with a id: " + id);
        }

        if (!Objects.equals(getAuthenticatedUser().getId(), comment.getUserId())) {
            throw new UserNotMatchException("User not matching with a comment");
        }

        if ((request.content() == null || request.content().trim().isEmpty()) && (request.media().isEmpty() || request.media().isEmpty())) {
            throw new CommentIsEmptyException("A comment must have either content or a media attachment.");
        }

        commentRedisTemplate.delete("comments:" + id);
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

        Comment updatedComment = commentDao.update(id, comment);

        SseEmitter emitter = emitters.get(comment.getPinId());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("updated-comment")
                        .data(updatedComment));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(comment.getPinId());
            }
        }

        return updatedComment;
    }

    @Override
    public SseEmitter createEmitter(long pinId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(pinId, emitter);

        emitter.onCompletion(() -> emitters.remove(pinId));
        emitter.onTimeout(() -> emitters.remove(pinId));

        return emitter;
    }

    /**
     * Retrieves a comment with little details, using database and cache
     * @param id The id of the comment to be found
     * @return A comment with specified id, either fetch from database or cache. If not comment are found, an exception is thrown
     */
    @Override
    public Comment findById(Long id, boolean fetchDetails) {
        String cacheKey = fetchDetails ? "comment:" + id + ":details" : "comment:" + id;

        // Retrieved cache comment from redis
        Comment cacheComment = commentRedisTemplate.opsForValue().get(cacheKey);

        if (cacheComment != null) {
            // Return cache comment if found
            return cacheComment;
        }

        // Fetch comment from the database
        Comment comment = Optional.ofNullable(commentDao.findById(id, fetchDetails))
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with a id: " + id));

        // Store in cache for 2 hours
        commentRedisTemplate.opsForValue().set(cacheKey, comment, Duration.ofHours(2));
        return comment;
    }

    /**
     * Retrieves a list of comment associated with specific pin, using both database and cache.
     * @param pinId The id of the pin whose comment are to be retrieved.
     * @param sortType The sort of the newest or oldest comment.
     * @param limit The maximum number of comment to be return.
     * @param offset The offset to paginate the comment result.
     * @return A list of the comment associated with specified pin ID, either fetch from database or cache. If no comment are found, an empty list is returned
     */
    @Override
    public List<Comment> findByPinId(Long pinId, SortType sortType, int limit, int offset) {
        String redisKeys = "pins:" + pinId + ":comments" + sortType;

        List<Comment> cachedComments = commentRedisTemplate.opsForList().range(redisKeys, offset, offset + limit - 1);
        if (cachedComments != null && !cachedComments.isEmpty()) {
            return cachedComments;
        }

        List<Comment> comments = commentDao.findByPinId(pinId, sortType, limit, offset);
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        commentRedisTemplate.opsForList().rightPushAll(redisKeys, comments);
        commentRedisTemplate.expire(redisKeys, Duration.ofHours(2));

        return comments;
    }

    /**
     * Delete comment by it ID.
     * @param id The id of comment to be deleted.
     */
    @Override
    public void deleteById(Long id) {
        // Fetch the comment from database
        Comment comment = commentDao.findById(id, false);
        if (comment == null) {
            // Throw exception if not found
            throw new CommentNotFoundException("Comment not found with a id: " + id);
        }

        if(!Objects.equals(getAuthenticatedUser().getId(),comment.getUserId())) {
            // Throw exception if user not own comment
            throw new UserNotMatchException("Authenticated user does not own the comment.");
        }

        commentDao.deleteById(comment.getId());
        commentRedisTemplate.delete("comment:" + id);
        commentRedisTemplate.delete("comment:" + id + ":details");
        commentRedisTemplate.delete("comment:" + id + ":subComments");
    }
}
