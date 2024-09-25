package com.app.DAO.Impl;

import com.app.Model.Comment;
import com.app.Model.Pin;
import com.app.Model.SubComment;
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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubCommentDaoImplTest {
    @Mock private JdbcTemplate template;
    @InjectMocks private SubCommentDaoImpl subCommentDao;

    private SubComment subComment;

    @BeforeEach
    void setUp() {
        subComment = SubComment.builder()
                .id(1L)
                .content("OK")
                .comment(Comment.builder()
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
                        .build())
                .user(User.builder()
                        .id(1L)
                        .username("test")
                        .email("test@gmail.com")
                        .password("test")
                        .build())
                .timestamp(Timestamp.from(Instant.now()))
                .build();
    }

    @Test
    void testSave() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Long generatedId = 1L;
        keyHolder.getKeyList().add(Map.of("GENERATED_KEY", generatedId));

        when(template.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
            .thenAnswer(invocation -> {
                KeyHolder passedKeyHolder = invocation.getArgument(1);
                passedKeyHolder.getKeyList().add(Map.of("GENERATED_KEY", generatedId));
                return 1;
            });

        SubComment saveSubComment = subCommentDao.save(subComment);

        assertNotNull(saveSubComment);
        assertEquals(generatedId,saveSubComment.getId());
        verify(template).update(any(PreparedStatementCreator.class),any(KeyHolder.class));
    }

    @Test
    void testFindAllByCommentId() {
        String sql = "SELECT sc.*, u.username, u.email, c.content AS comment_content " +
                "FROM sub_comments sc " +
                "JOIN users u ON sc.user_id = u.id " +
                "JOIN comments c ON sc.comment_id = c.id " +
                "WHERE sc.comment_id = ?";
        Long commentId = 1L;

        when(template.query(eq(sql),any(SubCommentRowMapper.class),eq(commentId))).thenReturn(List.of(subComment));

        List<SubComment> subCommentList = subCommentDao.findAllByCommentId(commentId);
        assertNotNull(subCommentList);
        verify(template).query(eq(sql),any(SubCommentRowMapper.class),eq(commentId));
    }

    @Test
    void testFindById() {
        String sql = "SELECT sc.*, u.username, u.email, c.content AS comment_content " +
                "FROM sub_comments sc " +
                "JOIN users u ON sc.user_id = u.id " +
                "JOIN comments c ON sc.comment_id = c.id " +
                "WHERE sc.id = ?";
        Long id = 1L;

        when(template.queryForObject(eq(sql),any(SubCommentRowMapper.class),eq(id))).thenReturn(subComment);

        SubComment foundSubComment = subCommentDao.findById(id);
        assertNotNull(foundSubComment);
        verify(template).queryForObject(eq(sql),any(SubCommentRowMapper.class),eq(id));
    }

    @Test
    void testDeleteById() {
        String sql = "DELETE FROM sub_comments WHERE id = ?";
        Long id = 1L;

        when(template.update(eq(sql),eq(id))).thenReturn(1);

        int row = subCommentDao.deleteById(id);
        assertEquals(1,row);
        verify(template).update(eq(sql),eq(id));
    }
}