package com.app.Service;

import com.app.DTO.request.CreateCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.Model.Comment;
import com.app.Model.SortType;

import java.util.List;

public interface CommentService {
    Comment save(CreateCommentRequest request);
    Comment update(Long id, UpdatedCommentRequest request);
    Comment findById(Long id, boolean fetchDetails);
    List<Comment> findByPinId(Long pinId, SortType sortType, int limit, int offset);
    void deleteById(Long id);
}
