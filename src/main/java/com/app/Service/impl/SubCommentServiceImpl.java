package com.app.Service.impl;

import com.app.DAO.CommentDao;
import com.app.DAO.MediaDao;
import com.app.DAO.SubCommentDao;
import com.app.DAO.UserDao;
import com.app.DTO.request.CreateSubCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.Model.*;
import com.app.Service.SubCommentService;
import com.app.exception.sub.CommentIsEmptyException;
import com.app.exception.sub.CommentNotFoundException;
import com.app.exception.sub.SubCommentNotFoundException;
import com.app.exception.sub.UserNotMatchException;
import com.app.message.producer.NotificationEventProducer;
import com.app.storage.FileManager;
import com.app.storage.MediaManager;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Qualifier("subCommentServiceImpl")
public class SubCommentServiceImpl implements SubCommentService {

  private final SubCommentDao subCommentDao;
  private final CommentDao commentDao;
  private final UserDao userDao;
  private final MediaDao mediaDao;
  private final NotificationEventProducer notificationEventProducer;

  private User getAuthenticationUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return userDao.findUserByUsername(authentication.getName());
  }

  @Override
  public SubComment save(CreateSubCommentRequest request) {

    String filename = MediaManager.generateUniqueFilename(request.file().getOriginalFilename());
    String extension = MediaManager.getFileExtension(request.file().getOriginalFilename());

    FileManager.save(request.file(), filename, extension);
    Media media =
        mediaDao.save(
            Media.builder().url(filename).mediaType(MediaType.fromExtension(extension)).build());

    Comment comment = commentDao.findById(request.commentId(), false);
    if (comment == null) {
      throw new CommentNotFoundException("Comment not found with a id: " + request.commentId());
    }

    SubComment subComment =
        SubComment.builder()
            .content(request.content())
            .comment(comment)
            .user(getAuthenticationUser())
            .media(media)
            .build();

    SubComment savedSubComment = subCommentDao.save(subComment);

    notificationEventProducer.send(
        Notification.builder()
            .userId(comment.getUserId())
            .message(
                getAuthenticationUser().getUsername()
                    + " replies on your comment "
                    + comment.getId())
            .build());
    return savedSubComment;
  }

  @Override
  public SubComment update(long id, UpdatedCommentRequest request) {

    SubComment subComment = subCommentDao.findById(id);
    if (subComment == null) {
      throw new SubCommentNotFoundException("Sub comment not found with a id: " + id);
    }

    if (!Objects.equals(getAuthenticationUser().getId(), subComment.getUser().getId())) {
      throw new UserNotMatchException("User does not match with sub comment");
    }

    if ((request.content() == null || request.content().trim().isEmpty())
        && (request.media().isEmpty() || request.media().isEmpty())) {
      throw new CommentIsEmptyException(
          "A comment must have either content or a media attachment.");
    }

    if (request.media() != null && !request.media().isEmpty()) {
      String extensionOfMedia = MediaManager.getFileExtension(subComment.getMedia().getUrl());

      String filename = MediaManager.generateUniqueFilename(request.media().getOriginalFilename());
      String extension = MediaManager.getFileExtension(request.media().getOriginalFilename());

      CompletableFuture.runAsync(
          () ->
              FileManager.delete(subComment.getMedia().getUrl(), extensionOfMedia)
                  .thenRunAsync(() -> FileManager.save(request.media(), filename, extension)));

      mediaDao.update(
          subComment.getMedia().getId(),
          Media.builder().url(filename).mediaType(MediaType.fromExtension(extension)).build());
    }

    if (request.content() != null && !request.content().trim().isEmpty()) {
      subComment.setContent(request.content());
    }

    return subCommentDao.update(id, subComment);
  }

  @Override
  public SubComment findById(long id) {
    return subCommentDao.findById(id);
  }

  @Override
  public List<SubComment> findAllByCommentId(
      long commentId, SortType sortType, int limit, int offset) {
    List<SubComment> subComments =
        subCommentDao.findAllByCommentId(commentId, sortType, limit, offset);
    if (subComments.isEmpty()) {
      return Collections.emptyList();
    }
    return subComments;
  }

  @Override
  public void deleteById(long id) {
    SubComment subComment = subCommentDao.findById(id);
    if (subComment == null) {
      throw new SubCommentNotFoundException("Sub comment not found with id: " + id);
    }

    if (!Objects.equals(subComment.getUser().getId(), getAuthenticationUser().getId())) {
      throw new UserNotMatchException("Authenticated user does not own the sub comment");
    }

    subCommentDao.deleteById(id);
  }
}
