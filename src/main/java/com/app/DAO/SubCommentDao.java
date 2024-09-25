package com.app.DAO;

import com.app.Model.SubComment;

import java.util.List;

public interface SubCommentDao {
    SubComment save(SubComment subComment);
    List<SubComment> findAllByCommentId(Long commentId);
    SubComment findById(Long id);
    int deleteById(Long id);
}
