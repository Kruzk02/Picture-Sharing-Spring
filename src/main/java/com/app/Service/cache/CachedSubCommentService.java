package com.app.Service.cache;

import com.app.DTO.request.CreateSubCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.Model.SortType;
import com.app.Model.SubComment;
import com.app.Service.SubCommentService;
import com.app.exception.sub.SubCommentNotFoundException;
import com.app.helper.CachedServiceHelper;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Primary
public class CachedSubCommentService extends CachedServiceHelper<SubComment>
    implements SubCommentService {

  private final SubCommentService subCommentService;

  public CachedSubCommentService(
      RedisTemplate<String, SubComment> subCommentRedisTemplate,
      @Qualifier("subCommentServiceImpl") SubCommentService subCommentService) {
    super(subCommentRedisTemplate);
    this.subCommentService = subCommentService;
  }

  @Override
  public SubComment save(CreateSubCommentRequest request) {
    var subComment = subCommentService.save(request);
    var cached =
        super.getOrLoad("subComment:" + subComment.getId(), () -> subComment, Duration.ofHours(2));
    return cached.orElse(subComment);
  }

  @Override
  public SubComment update(long id, UpdatedCommentRequest request) {
    var subComment = subCommentService.update(id, request);
    super.delete("subComment:" + subComment.getId());
    var cached =
        super.getOrLoad("subComment:" + subComment.getId(), () -> subComment, Duration.ofHours(2));
    return cached.orElse(subComment);
  }

  @Override
  public SubComment findById(long id) {
    var cached =
        super.getOrLoad(
            "subComment:" + id, () -> subCommentService.findById(id), Duration.ofHours(2));
    return cached.orElseThrow(
        () -> new SubCommentNotFoundException("Sub comment not found with a id: " + id));
  }

  @Override
  public List<SubComment> findAllByCommentId(
      long commentId, SortType sortType, int limit, int offset) {
    String redisKey = "comment:" + commentId + ":subComments:" + sortType;
    return super.getListOrLoad(
        redisKey,
        () -> subCommentService.findAllByCommentId(commentId, sortType, limit, offset),
        limit,
        offset,
        Duration.ofHours(2));
  }

  @Override
  public void deleteById(long id) {
    super.delete("subComment:" + id);
  }
}
