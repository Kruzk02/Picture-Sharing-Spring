package com.app.Service;

import com.app.DTO.request.CreateCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.Model.Comment;

import java.util.List;

public interface CommentService {
    Comment save(CreateCommentRequest request);
    Comment update(Long id, UpdatedCommentRequest request);
    Comment findBasicById(Long id);
    Comment findDetailsById(Long id);
    List<Comment> findByPinId(Long pinId, int limit, int offset);
    List<Comment> findNewestByPinId(Long pinId, int limit, int offset);
    List<Comment> findOldestByPinId(Long pinId, int limit, int offset);
    void deleteById(Long id);
    void deleteByPinId(Long pinId);
}
