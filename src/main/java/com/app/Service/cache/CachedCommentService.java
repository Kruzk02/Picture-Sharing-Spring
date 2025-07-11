package com.app.Service.cache;

import com.app.DTO.request.CreateCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.Model.Comment;
import com.app.Model.SortType;
import com.app.Service.CommentService;
import com.app.exception.sub.CommentNotFoundException;
import com.app.helper.CachedServiceHelper;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Primary
public class CachedCommentService extends CachedServiceHelper<Comment> implements CommentService {

  private final CommentService commentService;

  public CachedCommentService(
      RedisTemplate<String, Comment> commentRedisTemplate,
      @Qualifier("commentServiceImpl") CommentService commentService) {
    super(commentRedisTemplate);
    this.commentService = commentService;
  }

  @Override
  public Comment save(CreateCommentRequest request) {
    var comment = commentService.save(request);
    var cached =
        super.getOrLoad(
            "comment:" + comment.getId() + ":details", () -> comment, Duration.ofHours(2));
    return cached.orElse(comment);
  }

  @Override
  public Comment update(Long id, UpdatedCommentRequest request) {
    var comment = commentService.update(id, request);
    super.delete("comment:" + id + ":details");
    var cached =
        super.getOrLoad(
            "comment:" + comment.getId() + ":details", () -> comment, Duration.ofHours(2));
    return cached.orElse(comment);
  }

  @Override
  public SseEmitter createEmitter(long pinId) {
    return commentService.createEmitter(pinId);
  }

  @Override
  public Comment findById(Long id, boolean fetchDetails) {
    var cacheKey = fetchDetails ? "comment:" + id + ":details" : "comment:" + id;
    var cached =
        super.getOrLoad(
            cacheKey, () -> commentService.findById(id, fetchDetails), Duration.ofHours(2));
    return cached.orElseThrow(
        () -> new CommentNotFoundException("Comment not found with a id: " + id));
  }

  @Override
  public List<Comment> findByPinId(Long pinId, SortType sortType, int limit, int offset) {
    String redisKeys = "pins:" + pinId + ":comments:" + sortType;
    return super.getListOrLoad(
        redisKeys,
        () -> commentService.findByPinId(pinId, sortType, limit, offset),
        limit,
        offset,
        Duration.ofHours(2));
  }

  @Override
  public List<Comment> findByHashtag(String tag, int limit, int offset) {
    String redisKeys = "comments_hashtag:" + tag + ":limit:" + limit + ":offset:" + offset;
    return super.getListOrLoad(
        redisKeys,
        () -> commentService.findByHashtag(tag, limit, offset),
        limit,
        offset,
        Duration.ofHours(2));
  }

  @Override
  public void deleteById(Long id) {
    super.delete("comment:" + id + ":details");
    super.delete("comment:" + id);
  }
}
