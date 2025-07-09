package com.app.Service.impl;

import com.app.DAO.CommentDao;
import com.app.DAO.MediaDao;
import com.app.DAO.SubCommentDao;
import com.app.DAO.UserDao;
import com.app.DTO.request.CreateSubCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.Model.*;
import com.app.message.producer.NotificationEventProducer;
import com.app.storage.FileManager;
import com.app.storage.MediaManager;
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

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SubCommentServiceImplTest {

    @Mock private SubCommentDao subCommentDao;
    @Mock private CommentDao commentDao;
    @Mock private UserDao userDao;
    @Mock private MediaDao mediaDao;
    @Mock private NotificationEventProducer notificationEventProducer;
    @Mock private MultipartFile mockFile;

    @InjectMocks private SubCommentServiceImpl subCommentService;

    private Hashtag hashtag;
    private Media media;
    private User user;
    private Comment comment;
    private SubComment subComment;

    @BeforeEach
    void setUp() {
        media = Media.builder().id(1L).mediaType(MediaType.IMAGE).url("default_profile_picture.png").build();

        user = User.builder()
                .username("username")
                .email("email@gmail.com")
                .password("encodedPassword")
                .media(media)
                .gender(Gender.OTHER)
                .build();

        hashtag = Hashtag.builder()
                .id(1L)
                .tag("tag")
                .build();

        comment = Comment.builder()
                .id(1L)
                .pinId(1L)
                .userId(1L)
                .mediaId(1L)
                .content("content")
                .hashtags(List.of(hashtag))
                .build();

        subComment = SubComment.builder()
                .id(1L)
                .comment(comment)
                .user(user)
                .media(media)
                .content("WEWEWE")
                .build();
    }

    @Test
    void save_shouldSavedSubCommentSuccessfully() {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("username");
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(userDao.findUserByUsername("username")).thenReturn(user);

        Mockito.when(mockFile.getOriginalFilename()).thenReturn("test.jpg");

        var request = new CreateSubCommentRequest("content", mockFile, 1L);

        Mockito.when(mediaDao.save(Mockito.argThat(m ->
                m.getMediaType() != null &&
                        m.getUrl() != null)
        )).thenReturn(media);

        Mockito.when(commentDao.findById(1L, false)).thenReturn(comment);

        Mockito.when(subCommentDao.save(Mockito.argThat(sc ->
                sc.getComment() != null &&
                sc.getUser() != null &&
                sc.getContent() != null)
        )).thenReturn(subComment);

        var result = subCommentService.save(request);

        assertNotNull(result);
        assertEquals(subComment, result);

        Mockito.verify(notificationEventProducer).send(Mockito.any(Notification.class));
    }

    @Test
    void update_shouldUpdateSubComment_whenValidRequestAndMatchUser() {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("username");
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(userDao.findUserByUsername("username")).thenReturn(user);

        Mockito.when(subCommentDao.findById(1L)).thenReturn(subComment);

        Mockito.when(mockFile.getOriginalFilename()).thenReturn("new-file.jpg");

        var request = new UpdatedCommentRequest("content", mockFile, Set.of());

        try (
                MockedStatic<FileManager> fileManagerMocked = Mockito.mockStatic(FileManager.class);
                MockedStatic<MediaManager> mediaManagerMocked = Mockito.mockStatic(MediaManager.class);
        ) {
            mediaManagerMocked.when(() -> MediaManager.generateUniqueFilename("new-file.jpg")).thenReturn("new-file.jpg");
            mediaManagerMocked.when(() -> MediaManager.getFileExtension("new-file.jpg")).thenReturn("jpg");
            mediaManagerMocked.when(() -> MediaManager. getFileExtension("old-file.jpg")).thenReturn("jpg");

            fileManagerMocked.when(() -> FileManager.delete("old-file.jpg", "jpg")).thenReturn(CompletableFuture.completedFuture(null));
            fileManagerMocked.when(() -> FileManager.save(mockFile, "new-file.jpg", "jpg"))
                    .thenReturn(CompletableFuture.completedFuture(null));

            Media updatedMedia = Media.builder()
                    .id(1L)
                    .url("new-file.jpg")
                    .mediaType(MediaType.IMAGE)
                    .build();

            Mockito.when(mediaDao.update(Mockito.eq(1L), Mockito.argThat(m ->
                    m.getMediaType() != null &&
                            m.getUrl() != null))
            ).thenReturn(updatedMedia);

            Mockito.when(subCommentDao.update(Mockito.eq(1L), Mockito.argThat(sc ->
                    sc.getContent() != null &&
                    sc.getUser() != null &&
                    sc.getComment() != null)
            )).thenAnswer(invocation -> invocation.getArgument(1));

            var result = subCommentService.update(1L, request);
            assertNotNull(result);
            assertEquals(comment.getId(), result.getId());
        }
    }

    @Test
    void findById_shouldReturnSubComment() {
        Mockito.when(subCommentDao.findById(1L)).thenReturn(subComment);
        var result = subCommentService.findById(1L);

        assertNotNull(result);
        assertEquals(subComment, result);
    }

    @Test
    void findAllByCommentId_shouldReturnListOfSubComment() {
        Mockito.when(subCommentDao.findAllByCommentId(1L, SortType.NEWEST, 10, 0)).thenReturn(List.of(subComment));
        var result = subCommentService.findAllByCommentId(1L, SortType.NEWEST, 10, 0);

        assertNotNull(result);
        assertEquals(List.of(subComment), result);
    }

    @Test
    void deleteById_shouldDeleteExistingSubComment() {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("username");
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(userDao.findUserByUsername("username")).thenReturn(user);

        Mockito.when(subCommentDao.findById(1L)).thenReturn(subComment);

        subCommentService.deleteById(1L);

        Mockito.verify(subCommentDao).deleteById(1L);
    }
}