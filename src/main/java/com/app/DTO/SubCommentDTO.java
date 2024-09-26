package com.app.DTO;

import com.app.Model.Comment;
import com.app.Model.User;
import lombok.Data;

@Data
public class SubCommentDTO {
    private String content;
    private Comment comment;
    private User user;
}
