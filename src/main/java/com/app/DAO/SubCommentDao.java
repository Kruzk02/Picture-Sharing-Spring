package com.app.DAO;

import com.app.Model.SortType;
import com.app.Model.SubComment;
import java.util.List;

public interface SubCommentDao {
  SubComment save(SubComment subComment);

  SubComment update(Long id, SubComment subComment);

  List<SubComment> findAllByCommentId(Long commentId, SortType sortType, int limit, int offset);

  SubComment findById(Long id);

  int deleteById(Long id);
}
