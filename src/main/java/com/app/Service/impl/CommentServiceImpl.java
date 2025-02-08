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

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

        commentRedisTemplate.delete("comment_basic:" + id);
        commentRedisTemplate.delete("comment_detail:" + id);
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

        return commentDao.update(id, comment);
    }

    /**
     * Retrieves a comment with little details, using database and cache
     * @param id The id of the comment to be found
     * @return A comment with specified id, either fetch from database or cache. If not comment are found, an exception is thrown
     */
    @Override
    public Comment findBasicById(Long id) {
        String cacheKey = "comment_basic:" + id;

        // Retrieved cache comment from redis
        Comment cacheComment = (Comment) commentRedisTemplate.opsForValue().get(cacheKey);

        if (cacheComment != null) {
            // Return cache comment if found
            return cacheComment;
        }

        // Fetch comment from the database
        Comment comment = Optional.ofNullable(commentDao.findBasicById(id))
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with a id: " + id));

        // Store in cache for 2 hours
        commentRedisTemplate.opsForValue().set(cacheKey, comment, Duration.ofHours(2));
        return comment;
    }

    /**
     * Retrieves a comment with full details, using database and cache
     * @param id The id of the comment to be found
     * @return A comment with specified id, either fetch from database or cache. If not comment are found, an exception is thrown
     */
    @Override
    public Comment findDetailsById(Long id) {
        String cacheKey = "comment_detail:" + id;

        // Retrieved cache comment from redis
        Comment cacheComment = (Comment) commentRedisTemplate.opsForValue().get(cacheKey);

        if (cacheComment != null) {
            // return cache comment if found
            return cacheComment;
        }

        // Fetch comment from the database and handle null safely
        Comment comment = Optional.ofNullable(commentDao.findDetailsById(id))
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with a id: " + id));

        // Store in cache for 2 hours
        commentRedisTemplate.opsForValue().set(cacheKey, comment, Duration.ofHours(2));
        return comment;
    }

    /**
     * Retrieves a list of comment associated with specific pin, using both database and cache.
     * @param pinId The id of the pin whose comment are to be retrieved.
     * @param limit The maximum number of comment to be return.
     * @param offset The offset to paginate the comment result.
     * @return A list of the comment associated with specified pin ID, either fetch from database or cache. If no comment are found, an empty list is returned
     */
    @Override
    public List<Comment> findByPinId(Long pinId, int limit, int offset) {
        String redisKeys = "pins:" + pinId + ":comments";

        List<Comment> cachedComments = commentRedisTemplate.opsForList().range(redisKeys, offset, offset + limit - 1);
        if (cachedComments != null && !cachedComments.isEmpty()) {
            return cachedComments;
        }

        List<Comment> comments = commentDao.findByPinId(pinId, limit, offset);
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        commentRedisTemplate.opsForList().rightPushAll(redisKeys, comments);
        commentRedisTemplate.expire(redisKeys, Duration.ofHours(2));

        return comments;
    }

    /**
     * Retrieves a list of newest comments associated with a specific pin, using both database and cache
     * @param pinId The id of the pin whose comment are to be retrieved.
     * @param limit The maximum number of comments to be return.
     * @param offset The offset to paginate the comments result.
     * @return A list of newest comments associated with specific pin, either from fetch database or cache. if no comment are found, an empty list is returned.
     */
    @Override
    public List<Comment> findNewestByPinId(Long pinId, int limit, int offset) {
        String redisKeys = "pins:" + pinId + ":comments:newest";

        List<Comment> cachedComments = commentRedisTemplate.opsForList().range(redisKeys, offset, offset + limit - 1);
        if (cachedComments != null && !cachedComments.isEmpty()) {
            return cachedComments;
        }

        List<Comment> comments = commentDao.findNewestByPinId(pinId, limit, offset);
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        commentRedisTemplate.opsForList().rightPushAll(redisKeys, comments);
        commentRedisTemplate.expire(redisKeys, Duration.ofHours(2));

        return comments;
    }

    /**
     * Retrieves a list of oldest comments associated with a specific pin, using both database and cache
     * @param pinId The id of the pin whose comment are to be retrieved.
     * @param limit The maximum number of comments to be return.
     * @param offset The offset to paginate the comments result.
     * @return A list of oldest comments associated with specific pin, either from fetch database or cache. if no comment are found, an empty list is returned.
     */
    @Override
    public List<Comment> findOldestByPinId(Long pinId, int limit, int offset) {
        String redisKeys = "pins:" + pinId + ":comments";

        List<Comment> cachedComments = commentRedisTemplate.opsForList().range(redisKeys, offset, offset + limit - 1);
        if (cachedComments != null && !cachedComments.isEmpty()) {
            return cachedComments;
        }

        List<Comment> comments = commentDao.findOldestByPinId(pinId, limit, offset);
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
        Comment comment = commentDao.findBasicById(id);
        if (comment == null) {
            // Throw exception if not found
            throw new CommentNotFoundException("Comment not found with a id: " + id);
        }

        if(!Objects.equals(getAuthenticatedUser().getId(),comment.getUserId())) {
            // Throw exception if user not own comment
            throw new UserNotMatchException("Authenticated user does not own the comment.");
        }

        commentDao.deleteById(comment.getId());
        commentRedisTemplate.delete("comment_basic:" + id);
        commentRedisTemplate.delete("comment_detail:" + id);
        commentRedisTemplate.delete("comments:" + id);
        commentRedisTemplate.delete("comment:" + id + ":subComments");
        commentRedisTemplate.delete("comment:" + id + ":subComments:newest");
    }
}
