package com.app.DAO.Impl;

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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MediaDaoImplTest {

    @InjectMocks private MediaDaoImpl mediaDao;
    @Mock private JdbcTemplate jdbcTemplate;

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
    void save_shouldInsertMedia() {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        keyHolder.getKeyList().add(Map.of("GENERATED_KEY", 1L));

        Mockito.when(jdbcTemplate.update(Mockito.any(PreparedStatementCreator.class), Mockito.any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder kh = invocation.getArgument(1);
                    kh.getKeyList().add(Map.of("GENERATED_KEY", 1L));
                    return 1;
                });

        var result = mediaDao.save(media);

        assertNotNull(result);
        assertEquals(media, result);
    }

    @Test
    void save_shouldReturnNull_whenInsertFails() {
        Mockito.when(jdbcTemplate.update(Mockito.any(PreparedStatementCreator.class), Mockito.any(KeyHolder.class)))
                .thenReturn(0);

        var result = mediaDao.save(media);
        assertNull(result);
    }

    @Test
    void findById_shouldReturnMedia_whenMediaExists() {
        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.eq("SELECT id, url, media_type FROM media WHERE id = ?"),
                Mockito.any(RowMapper.class),
                Mockito.eq(1L)
        )).thenReturn(media);

        var result = mediaDao.findById(1L);
        assertNotNull(result);
        assertEquals(media, result);

        Mockito.verify(jdbcTemplate).queryForObject(
                Mockito.eq("SELECT id, url, media_type FROM media WHERE id = ?"),
                Mockito.any(RowMapper.class),
                Mockito.eq(1L)
        );
    }

    @Test
    void findById_shouldThrowException_whenMediaDoesNotExists() {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.any(RowMapper.class), Mockito.eq(1L))).thenThrow(new EmptyResultDataAccessException(1));
        assertThrows(MediaNotFoundException.class, () -> mediaDao.findById(1L));
    }

    @Test
    void findByCommentId_shouldReturnMedia_whenMediaExists() {
        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.eq("SELECT m.id, m.url, m.media_type FROM media m " +
                        "INNER JOIN comments c ON m.id = c.media_id " +
                        "WHERE c.id = ?"),
                Mockito.any(RowMapper.class),
                Mockito.eq(1L)
        )).thenReturn(media);

        var result = mediaDao.findByCommentId(1L);

        assertNotNull(result);
        assertEquals(media, result);
        Mockito.verify(jdbcTemplate).queryForObject(
                Mockito.eq("SELECT m.id, m.url, m.media_type FROM media m " +
                        "INNER JOIN comments c ON m.id = c.media_id " +
                        "WHERE c.id = ?"),
                Mockito.any(RowMapper.class),
                Mockito.eq(1L)
        );
    }

    @Test
    void findByCommentId_shouldThrowException_whenMediaDoesNotExists() {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.any(RowMapper.class), Mockito.eq(1L))).thenThrow(new EmptyResultDataAccessException(1));
        assertThrows(MediaNotFoundException.class, () -> mediaDao.findByCommentId(1L));
    }

    @Test
    void deleteById_shouldDeleteMedia_whenMediaExists() {
        Mockito.when(jdbcTemplate.update(
                Mockito.eq("DELETE FROM media WHERE id = ?"),
                Mockito.eq(1L)
        )).thenReturn(1);

        var result = mediaDao.deleteById(1L);

        assertEquals(1L, result);
    }

    @Test
    void deleteById_shouldThrowException_whenMediaDoesNotExists() {
        Mockito.when(jdbcTemplate.update(
                Mockito.anyString(),
                Mockito.eq(1L)
        )).thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(MediaNotFoundException.class, () -> mediaDao.deleteById(1L));
    }
}