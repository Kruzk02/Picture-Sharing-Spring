package com.app.Service;

import com.app.DTO.request.CreateMediaRequest;
import com.app.DTO.request.UpdatedMediaRequest;
import com.app.Model.Media;

import java.util.List;

public interface MediaService {
    Media save(CreateMediaRequest request);
    Media update(Long id, UpdatedMediaRequest request);
    Media findById(Long id);
    Media findByCommentId(Long commentId);
    void deleteById(Long id);
}
