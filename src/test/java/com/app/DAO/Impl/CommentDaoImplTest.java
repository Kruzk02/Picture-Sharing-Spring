package com.app.DAO.Impl;

import com.app.Model.Comment;
import com.app.Model.Pin;
import com.app.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentDaoImplTest {

    @Mock private JdbcTemplate template;

    @InjectMocks private CommentDaoImpl commentDao;

    private Comment comment;

    @BeforeEach
    void setUp() {
        comment = Comment.builder()
                .id(1L)
                .content("HELLO WORLD")
                .user(User.builder()
                        .id(1L)
                        .username("test")
                        .email("test@gmail.com")
                        .password("test")
                        .build())
                .pin(Pin.builder()
                        .id(1L)
                        .description("NOPE")
                        .fileName("YES")
                        .image_url("/upload")
                        .userId(1L)
                        .build())
                .build();
    }

    @Test
    void testSave() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        long generatedId = 1L;
        keyHolder.getKeyList().add(Map.of("GENERATED_KEY", generatedId));

        when(template.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder passedKeyHolder = invocation.getArgument(1);
                    passedKeyHolder.getKeyList().add(Map.of("GENERATED_KEY", generatedId));
                    return 1;
                });


       Comment savedComment = commentDao.save(comment);

        assertNotNull(savedComment);
        assertEquals(generatedId, savedComment.getId());
        verify(template).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
    }

    @Test
    void testFindById() {
        String sql = "SELECT * FROM comments WHERE id = ?";
        Long id = 1L;

        when(template.queryForObject(eq(sql),any(CommentRowMapper.class),eq(id))).thenReturn(comment);

        commentDao.findById(id);

        verify(template).queryForObject(eq(sql),any(CommentRowMapper.class),eq(id));
    }

    @Test
    void testFindByPinId() {
        String sql = "SELECT * FROM comments WHERE pin_id = ?";
        Long pinId = 1L;

        when(template.query(eq(sql),any(CommentRowMapper.class),eq(pinId))).thenReturn(List.of(comment));

        commentDao.findByPinId(pinId);

        verify(template).query(eq(sql),any(CommentRowMapper.class),eq(pinId));
    }

    @Test
    void testDeleteById() {
        String sql = "DELETE FROM comments WHERE id = ?";
        Long id = 1L;

        when(template.update(eq(sql),eq(id))).thenReturn(1);

        commentDao.deleteById(id);

        verify(template).update(eq(sql),eq(id));
    }
}