package com.app.Service;

import com.app.DTO.request.CreateSubCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.Model.SortType;
import com.app.Model.SubComment;
import java.util.List;

public interface SubCommentService {
  SubComment save(CreateSubCommentRequest request);

  SubComment update(long id, UpdatedCommentRequest request);

  SubComment findById(long id);

  List<SubComment> findAllByCommentId(long commentId, SortType sortType, int limit, int offset);

  void deleteById(long id);
}
