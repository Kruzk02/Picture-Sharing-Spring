package com.app.Service.impl;

import com.app.DAO.MediaDao;
import com.app.Model.Media;
import com.app.Model.MediaType;
import com.app.exception.sub.MediaNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    @Mock private MediaDao mediaDao;

    @InjectMocks private MediaServiceImpl mediaService;

    private Media media;

    @BeforeEach
    void setUp() {
        media = Media.builder()
                .id(1L)
                .url("url")
                .mediaType(MediaType.IMAGE)
                .build();
    }

    @Test
    void findById_shouldReturnMedia() {
        Mockito.when(mediaDao.findById(1L)).thenReturn(media);
        var result = mediaService.findById(1L);

        assertNotNull(result);
        assertEquals(media, result);
    }

    @Test
    void findById_shouldThrowException() {
        Mockito.when(mediaDao.findById(1L)).thenReturn(null);
        var ex = assertThrows(MediaNotFoundException.class, () -> mediaService.findById(1L));
        assertEquals("Media not found with a id: 1", ex.getMessage());
    }
}