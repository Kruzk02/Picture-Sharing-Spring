package com.app.Service.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.app.DAO.*;
import com.app.DTO.request.CreateCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.Model.*;
import com.app.message.producer.NotificationEventProducer;
import com.app.storage.FileManager;
import com.app.storage.MediaManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

  @Mock private CommentDao commentDao;
  @Mock private UserDao userDao;
  @Mock private PinDao pinDao;
  @Mock private MediaDao mediaDao;
  @Mock private HashtagDao hashtagDao;
  @Mock private Map<Long, SseEmitter> emitters;
  @Mock private NotificationEventProducer notificationEventProducer;
  @Mock private MultipartFile mockFile;

  @InjectMocks private CommentServiceImpl commentService;

  private Comment comment;
  private User user;
  private Media media;
  private Pin pin;
  private Hashtag hashtag;

  @BeforeEach
  void setUp() {
    hashtag = Hashtag.builder().id(1L).tag("tag").build();
    user =
        User.builder()
            .id(1L)
            .username("username")
            .email("email@gmail.com")
            .password("encodedPassword")
            .enable(false)
            .gender(Gender.MALE)
            .build();
    comment =
        Comment.builder()
            .id(1L)
            .pinId(1L)
            .userId(1L)
            .mediaId(1L)
            .content("content")
            .hashtags(List.of(hashtag))
            .build();
    media = Media.builder().id(1L).url("filename").mediaType(MediaType.IMAGE).build();

    pin =
        Pin.builder()
            .id(1L)
            .description("description")
            .userId(1L)
            .mediaId(1L)
            .hashtags(List.of(hashtag))
            .build();
  }

  @Test
  void save_shouldSavedCommentSuccessfully() {
    Authentication auth = Mockito.mock(Authentication.class);
    Mockito.when(auth.getName()).thenReturn("username");
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(securityContext);
    Mockito.when(userDao.findUserByUsername("username")).thenReturn(user);

    Mockito.when(mockFile.getOriginalFilename()).thenReturn("test.jpg");

    var request = new CreateCommentRequest("content", 1L, mockFile, Set.of("tag1", "tag2"));
    Mockito.when(hashtagDao.findByTag(Set.of("tag1", "tag2"))).thenReturn(new HashMap<>());
    Mockito.when(hashtagDao.save(Mockito.argThat(ht -> ht.getTag() != null)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Mockito.when(
            mediaDao.save(Mockito.argThat(m -> m.getMediaType() != null && m.getUrl() != null)))
        .thenReturn(media);

    Mockito.when(pinDao.findById(1L, false)).thenReturn(pin);

    Mockito.when(
            commentDao.save(
                Mockito.argThat(
                    c ->
                        c.getContent() != null
                            && !c.getHashtags().isEmpty()
                            && c.getUserId() != 0
                            && c.getPinId() != 0)))
        .thenReturn(comment);

    var result = commentService.save(request);

    assertNotNull(result);
    assertEquals(comment.getId(), result.getId());
    Mockito.verify(commentDao)
        .save(
            Mockito.argThat(
                c ->
                    c.getContent() != null
                        && !c.getHashtags().isEmpty()
                        && c.getUserId() != 0
                        && c.getPinId() != 0));
    Mockito.verify(mediaDao)
        .save(Mockito.argThat(m -> m.getMediaType() != null && m.getUrl() != null));

    Mockito.verify(notificationEventProducer).send(Mockito.any(Notification.class));
  }

  @Test
  void update_shouldUpdateComment_whenValidRequestAndMatchUser() {

    Mockito.when(commentDao.findById(1L, true)).thenReturn(comment);
    Mockito.when(userDao.findUserByUsername("username")).thenReturn(user);

    Mockito.when(mockFile.getOriginalFilename()).thenReturn("new-file.jpg");

    var request = new UpdatedCommentRequest("content", mockFile, Set.of("tag1", "tag2"));

    Mockito.when(mediaDao.findByCommentId(1L)).thenReturn(media);

    Mockito.when(hashtagDao.save(Mockito.argThat(ht -> ht.getTag() != null)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    try (MockedStatic<FileManager> fileManagerMocked = Mockito.mockStatic(FileManager.class);
        MockedStatic<MediaManager> mediaManagerMocked = Mockito.mockStatic(MediaManager.class); ) {
      mediaManagerMocked
          .when(() -> MediaManager.generateUniqueFilename("new-file.jpg"))
          .thenReturn("new-file.jpg");
      mediaManagerMocked
          .when(() -> MediaManager.getFileExtension("new-file.jpg"))
          .thenReturn("jpg");
      mediaManagerMocked
          .when(() -> MediaManager.getFileExtension("old-file.jpg"))
          .thenReturn("jpg");

      fileManagerMocked
          .when(() -> FileManager.delete("old-file.jpg", "jpg"))
          .thenReturn(CompletableFuture.completedFuture(null));
      fileManagerMocked
          .when(() -> FileManager.save(mockFile, "new-file.jpg", "jpg"))
          .thenReturn(CompletableFuture.completedFuture(null));

      Media updatedMedia =
          Media.builder().id(1L).url("new-file.jpg").mediaType(MediaType.IMAGE).build();

      Mockito.when(
              mediaDao.update(
                  Mockito.eq(1L),
                  Mockito.argThat(m -> m.getMediaType() != null && m.getUrl() != null)))
          .thenReturn(updatedMedia);

      Mockito.when(
              commentDao.update(
                  Mockito.eq(1L),
                  Mockito.argThat(
                      c ->
                          c.getContent() != null
                              && !c.getHashtags().isEmpty()
                              && c.getUserId() != 0
                              && c.getPinId() != 0)))
          .thenAnswer(invocation -> invocation.getArgument(1));

      var result = commentService.update(1L, request);
      assertNotNull(result);
      assertEquals(comment.getId(), result.getId());

      Mockito.verify(mediaDao)
          .update(
              Mockito.eq(1L), Mockito.argThat(m -> m.getMediaType() != null && m.getUrl() != null));

      Mockito.verify(commentDao)
          .update(
              Mockito.eq(1L),
              Mockito.argThat(
                  c ->
                      c.getContent() != null
                          && !c.getHashtags().isEmpty()
                          && c.getUserId() != 0
                          && c.getPinId() != 0));

      Mockito.verify(emitters).get(result.getId());
    }
  }

  @Test
  void findById_shouldReturnComment() {
    Mockito.when(commentDao.findById(1L, true)).thenReturn(comment);
    var result = commentService.findById(1L, true);

    assertNotNull(result);
    assertEquals(comment, result);
  }

  @Test
  void findByPinId_shouldReturnListOfComment() {
    Mockito.when(commentDao.findByPinId(1L, SortType.NEWEST, 10, 0)).thenReturn(List.of(comment));
    var result = commentService.findByPinId(1L, SortType.NEWEST, 10, 0);

    assertNotNull(result);
    assertEquals(List.of(comment), result);
  }

  @Test
  void findByHashTag_shouldReturnListOfComment() {
    Mockito.when(commentDao.findByHashtag("tag", 10, 0)).thenReturn(List.of(comment));
    var result = commentService.findByHashtag("tag", 10, 0);

    assertNotNull(result);
    assertEquals(List.of(comment), result);
  }

  @Test
  void deleteById_shouldDeleteExistingComment() {
    Authentication auth = Mockito.mock(Authentication.class);
    Mockito.when(auth.getName()).thenReturn("username");
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(securityContext);
    Mockito.when(userDao.findUserByUsername("username")).thenReturn(user);

    Mockito.when(commentDao.findById(1L, false)).thenReturn(comment);

    commentService.deleteById(1L);

    Mockito.verify(commentDao).deleteById(1L);
  }
}
