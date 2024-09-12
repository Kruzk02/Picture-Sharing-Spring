package com.app.DAO;

import com.app.Model.Comment;

import java.util.List;

public interface CommentDao {

    Comment save(Comment comment);
    Comment findById(Long id);
    List<Comment> findByPinId(Long pinId);
    int deleteById(Long id);
}
