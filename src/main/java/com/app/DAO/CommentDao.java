package com.app.DAO;

import com.app.Model.Comment;

public interface CommentDao {

    Comment save(Comment comment);
    Comment findById(Long id);
    int deleteById(Long id);
}
