package com.app.DTO.response;

import com.app.Model.Comment;

import java.util.List;

public record GetCommentResponse(List<Comment> comments) { }
