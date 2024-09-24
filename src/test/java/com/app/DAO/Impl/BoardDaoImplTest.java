package com.app.DAO.Impl;

import com.app.Model.Board;
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
import org.springframework.jdbc.core.RowMapper;
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
class BoardDaoImplTest {
    @Mock private JdbcTemplate template;
    @InjectMocks private BoardDaoImpl boardDao;

    private Board board;

    @BeforeEach
    void setUp() {
        board = Board.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .username("test")
                        .email("test@gmail.com")
                        .password("test")
                        .build())
                .name("1")
                .pins(List.of(Pin.builder()
                        .id(1L)
                        .description("NOPE")
                        .fileName("YES")
                        .image_url("/upload")
                        .userId(1L)
                        .build()))
                .build();
    }

    @Test
    void testSave() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        long generatedId = 1L;
        keyHolder.getKeyList().add(Map.of("GENERATED_KEY", generatedId));

        when(template.update(any(PreparedStatementCreator.class),any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder passedKeyHolder = invocation.getArgument(1);
                    passedKeyHolder.getKeyList().add(Map.of("GENERATED_KEY", generatedId));
                    return 1;
                });

        Board savedBoard = boardDao.save(board,1L);
        assertNotNull(savedBoard);
        assertEquals(generatedId,savedBoard.getId());
        verify(template).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
    }

    @Test
    void testFindById() {
        String sql = "SELECT b.id AS board_id, b.board_name,b.user_id, p.id AS pin_id, p.file_name, p.image_url, p.description " +
                "FROM boards b " +
                "JOIN board_pin bp ON b.id = bp.board_id " +
                "JOIN pins p ON bp.pin_id = p.id " +
                "WHERE b.id = ?";
        Long id = 1L;

        when(template.queryForObject(eq(sql),any(BoardRowMapper.class),eq(id))).thenReturn(board);

        boardDao.findById(id);

        verify(template).queryForObject(eq(sql),any(BoardRowMapper.class),eq(id));
    }

    @Test
    void testFindAllByUserId() {
        String sql = "SELECT * from boards WHERE user_id = ?";
        Long userId = 1L;

        when(template.query(eq(sql),any(RowMapper.class),eq(userId))).thenReturn(List.of(board));

        boardDao.findAllByUserId(userId);

        verify(template).query(eq(sql),any(RowMapper.class),eq(userId));
    }

    @Test
    void testDeleteById() {
        String sql = "DELETE FROM boards WHERE id = ?";
        Long id = 1L;

        when(template.update(eq(sql),eq(id))).thenReturn(1);

        boardDao.deleteById(id);

        verify(template).update(eq(sql),eq(id));
    }
}