package com.app.Service.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.app.DAO.HashtagDao;
import com.app.DAO.MediaDao;
import com.app.DAO.PinDao;
import com.app.DAO.UserDao;
import com.app.DTO.request.PinRequest;
import com.app.Model.*;
import com.app.exception.sub.PinNotFoundException;
import com.app.storage.FileManager;
import com.app.storage.MediaManager;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class PinServiceImplTest {

  @Mock private PinDao pinDao;
  @Mock private UserDao userDao;
  @Mock private MediaDao mediaDao;
  @Mock private HashtagDao hashtagDao;
  @Mock private MultipartFile mockFile;

  @InjectMocks private PinServiceImpl pinService;

  private Pin pin;
  private User user;
  private Hashtag hashtag;
  private Media media;

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
    pin =
        Pin.builder()
            .id(1L)
            .description("description")
            .userId(1L)
            .mediaId(1L)
            .hashtags(List.of(hashtag))
            .build();

    media = Media.builder().id(1L).url("filename").mediaType(MediaType.IMAGE).build();
  }

  @Test
  void getAllPins_shouldReturnListOfPin() {
    Mockito.when(pinDao.getAllPins(SortType.NEWEST, 10, 0)).thenReturn(List.of(pin));
    var result = pinService.getAllPins(SortType.NEWEST, 10, 0);

    assertNotNull(result);
    assertEquals(List.of(pin), result);
  }

  @Test
  void getAllPinsByHashTag_shouldReturnListOfPin() {
    Mockito.when(pinDao.getAllPinsByHashtag("tag", 10, 0)).thenReturn(List.of(pin));
    var result = pinService.getAllPinsByHashtag("tag", 10, 0);

    assertNotNull(result);
    assertEquals(List.of(pin), result);
  }

  @Test
  void save_shouldSavePinSuccessfully() {

    Authentication auth = Mockito.mock(Authentication.class);
    Mockito.when(auth.getName()).thenReturn("username");
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(securityContext);
    Mockito.when(userDao.findUserByUsername("username")).thenReturn(user);

    Mockito.when(mockFile.isEmpty()).thenReturn(false);
    Mockito.when(mockFile.getOriginalFilename()).thenReturn("test.jpg");

    PinRequest request = new PinRequest("Description", mockFile, Set.of("tag1", "tag2"));
    Mockito.when(hashtagDao.findByTag(Set.of("tag1", "tag2"))).thenReturn(new HashMap<>());
    Mockito.when(hashtagDao.save(Mockito.argThat(ht -> ht.getTag() != null)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Mockito.when(
            mediaDao.save(Mockito.argThat(m -> m.getMediaType() != null && m.getUrl() != null)))
        .thenReturn(media);

    Mockito.when(
            pinDao.save(
                Mockito.argThat(
                    p ->
                        !p.getHashtags().isEmpty()
                            && p.getDescription() != null
                            && p.getUserId() != 0
                            && p.getMediaId() != 0)))
        .thenReturn(pin);

    Pin result = pinService.save(request);

    assertNotNull(result);
    assertEquals(pin.getId(), result.getId());
    Mockito.verify(pinDao)
        .save(
            Mockito.argThat(
                p ->
                    !p.getHashtags().isEmpty()
                        && p.getDescription() != null
                        && p.getUserId() != 0
                        && p.getMediaId() != 0));
    Mockito.verify(mediaDao)
        .save(Mockito.argThat(m -> m.getMediaType() != null && m.getUrl() != null));
  }

  @Test
  void update_ShouldUpdatePin_WhenValidRequestAndMatchingUser() {
    String newFilename = "new-file.jpg";
    String newExtension = "jpg";

    Media existingMedia =
        Media.builder().id(1L).url("old-file.png").mediaType(MediaType.IMAGE).build();

    Mockito.when(pinDao.findById(1L, false)).thenReturn(pin);
    Mockito.when(userDao.findUserByUsername("username")).thenReturn(user);

    Mockito.when(mockFile.isEmpty()).thenReturn(false);
    Mockito.when(mockFile.getOriginalFilename()).thenReturn("new-file.jpg");

    PinRequest pinRequest = new PinRequest("New description", mockFile, Set.of("tag1"));

    Mockito.when(mediaDao.findById(pin.getMediaId())).thenReturn(existingMedia);

    Mockito.when(hashtagDao.findByTag(Set.of("tag1"))).thenReturn(Map.of());
    Mockito.when(hashtagDao.save(Mockito.argThat(ht -> ht.getTag() != null)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    try (MockedStatic<FileManager> fileManagerMock = Mockito.mockStatic(FileManager.class);
        MockedStatic<MediaManager> mediaManagerMock = Mockito.mockStatic(MediaManager.class)) {
      mediaManagerMock
          .when(() -> MediaManager.generateUniqueFilename("new-file.jpg"))
          .thenReturn(newFilename);
      mediaManagerMock
          .when(() -> MediaManager.getFileExtension("new-file.jpg"))
          .thenReturn(newExtension);
      mediaManagerMock.when(() -> MediaManager.getFileExtension("old-file.png")).thenReturn("png");

      fileManagerMock
          .when(() -> FileManager.delete(Mockito.anyString(), Mockito.anyString()))
          .thenReturn(CompletableFuture.completedFuture(null));
      fileManagerMock
          .when(() -> FileManager.save(Mockito.any(), Mockito.anyString(), Mockito.anyString()))
          .thenReturn(CompletableFuture.completedFuture(null));

      Media updatedMedia =
          Media.builder().id(1L).url(newFilename).mediaType(MediaType.IMAGE).build();

      Mockito.when(
              mediaDao.update(
                  Mockito.eq(1L),
                  Mockito.argThat(m -> m.getMediaType() != null && m.getUrl() != null)))
          .thenReturn(updatedMedia);

      Mockito.when(
              pinDao.update(
                  Mockito.eq(1L),
                  Mockito.argThat(
                      p ->
                          !p.getHashtags().isEmpty()
                              && p.getDescription() != null
                              && p.getUserId() != 0
                              && p.getMediaId() != 0)))
          .thenAnswer(invocation -> invocation.getArgument(1));

      Pin updatedPin = pinService.update(1L, pinRequest);

      assertEquals("New description", updatedPin.getDescription());
      assertEquals(user.getId(), updatedPin.getUserId());

      Mockito.verify(mediaDao)
          .update(
              Mockito.eq(1L), Mockito.argThat(m -> m.getMediaType() != null && m.getUrl() != null));
      Mockito.verify(pinDao)
          .update(
              Mockito.eq(1L),
              Mockito.argThat(
                  p ->
                      p.getId() != null
                          && !p.getHashtags().isEmpty()
                          && p.getDescription() != null
                          && p.getUserId() != 0
                          && p.getMediaId() != 0));
      Mockito.verify(hashtagDao).save(Mockito.argThat(ht -> ht.getTag() != null));
    }
  }

  @Test
  void findById_shouldReturnPin() {
    Mockito.when(pinDao.findById(1L, false)).thenReturn(pin);
    var result = pinService.findById(1L, false);

    assertNotNull(result);
    assertEquals(pin, result);
  }

  @Test
  void findPinByUserId_shouldReturnListOfPin() {
    Mockito.when(pinDao.findPinByUserId(1L, 10, 0)).thenReturn(List.of(pin));
    var result = pinService.findPinByUserId(1L, 10, 0);

    assertNotNull(result);
    assertEquals(List.of(pin), result);
  }

  @Test
  void deleteById_shouldDeleteExistingPin() throws IOException {
    Mockito.when(userDao.findUserByUsername("username")).thenReturn(user);
    Mockito.when(pinDao.findById(1L, false)).thenReturn(pin);
    Mockito.when(mediaDao.findById(1L)).thenReturn(media);

    pinService.delete(1L);

    Mockito.verify(mediaDao).findById(1L);
    Mockito.verify(pinDao).deleteById(1L);
  }

  @Test
  void testDeleteById_PinNotFound() {
    Mockito.when(pinDao.findById(2L, false)).thenReturn(null);

    PinNotFoundException ex = assertThrows(PinNotFoundException.class, () -> pinService.delete(2L));
    assertEquals("Pin not found with a id: 2", ex.getMessage());
  }
}
