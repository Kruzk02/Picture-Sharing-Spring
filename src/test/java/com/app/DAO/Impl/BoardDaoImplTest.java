package com.app.DAO.Impl;

import static org.junit.jupiter.api.Assertions.*;

import com.app.Model.*;
import com.app.exception.sub.BoardNotFoundException;
import java.util.*;
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
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@ExtendWith(MockitoExtension.class)
class BoardDaoImplTest {

  @InjectMocks private BoardDaoImpl boardDao;
  @Mock private JdbcTemplate jdbcTemplate;

  private Board board;
  private Pin pin;

  @BeforeEach
  void setUp() {
    pin =
        Pin.builder()
            .id(1L)
            .description("description")
            .userId(1L)
            .mediaId(1L)
            .hashtags(List.of(Hashtag.builder().id(1L).tag("tag").build()))
            .build();
    board =
        Board.builder()
            .id(1L)
            .name("name")
            .user(
                User.builder()
                    .id(1L)
                    .username("username")
                    .email("email@gmail.com")
                    .password("HashedPassword")
                    .gender(Gender.MALE)
                    .media(Media.builder().id(1L).mediaType(MediaType.IMAGE).url("NO").build())
                    .roles(
                        List.of(
                            Role.builder()
                                .id(2L)
                                .name("ROLE_USER")
                                .privileges(
                                    List.of(Privilege.builder().id(2L).name("READ").build()))
                                .build()))
                    .bio("bio")
                    .enable(false)
                    .build())
            .pins(new ArrayList<>(Collections.singletonList(pin)))
            .build();
  }

  @Test
  void save_shouldInsertBoard() {
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    keyHolder.getKeyList().add(Map.of("GENERATED_KEY", 1L));

    Mockito.when(
            jdbcTemplate.update(
                Mockito.any(PreparedStatementCreator.class), Mockito.any(KeyHolder.class)))
        .thenAnswer(
            invocation -> {
              KeyHolder kh = invocation.getArgument(1);
              kh.getKeyList().add(Map.of("GENERATED_KEY", 1L));
              return 1;
            });

    var result = boardDao.save(board);
    assertNotNull(result);
    assertEquals(board, result);
  }

  @Test
  void save_shouldThrowException_whenInsertFail() {
    Mockito.when(
            jdbcTemplate.update(
                Mockito.any(PreparedStatementCreator.class), Mockito.any(KeyHolder.class)))
        .thenReturn(0);

    assertThrows(RuntimeException.class, () -> boardDao.save(board));
  }

  @Test
  void addPinToBoard_shouldInsertPinToBoard() {
    Mockito.when(
            jdbcTemplate.update(
                Mockito.eq("INSERT INTO board_pin (board_id, pin_id) VALUES(?,?)"),
                Mockito.eq(1L),
                Mockito.eq(1L)))
        .thenReturn(1);

    Board result = boardDao.addPinToBoard(pin, board);

    assertNotNull(result);
    assertTrue(result.getPins().contains(pin));
    Mockito.verify(jdbcTemplate)
        .update("INSERT INTO board_pin (board_id, pin_id) VALUES(?,?)", 1L, 1L);
  }

  @Test
  void deletePinFromBoard() {
    Mockito.when(
            jdbcTemplate.update(
                Mockito.eq("DELETE FROM board_pin WHERE board_id = ? AND pin_id = ?"),
                Mockito.eq(1L),
                Mockito.eq(1L)))
        .thenReturn(1);

    Board result = boardDao.deletePinFromBoard(pin, board);

    assertNotNull(result);
    assertFalse(result.getPins().contains(pin));
    Mockito.verify(jdbcTemplate)
        .update("DELETE FROM board_pin WHERE board_id = ? AND pin_id = ?", 1L, 1L);
  }

  @Test
  void findById_shouldReturnBoard_whenBoardExists() {
    Mockito.when(
            jdbcTemplate.query(
                Mockito.eq(
                    "SELECT b.id AS board_id, b.board_name, b.create_at, "
                        + "u.id AS user_id, u.username, "
                        + "p.id AS pin_id, p.media_id, p.user_id AS pin_user_id, p.created_at AS pin_created_at "
                        + "FROM boards b "
                        + "JOIN users u ON b.user_id = u.id "
                        + "LEFT JOIN board_pin bp ON b.id = bp.board_id "
                        + "LEFT JOIN pins p ON p.id = bp.pin_id "
                        + "WHERE b.id = ?"),
                Mockito.any(BoardResultSetExtractor.class),
                Mockito.eq(1L)))
        .thenReturn(List.of(board));

    var result = boardDao.findById(1L);
    assertNotNull(result);
    assertEquals(board, result);

    Mockito.verify(jdbcTemplate)
        .query(
            Mockito.eq(
                "SELECT b.id AS board_id, b.board_name, b.create_at, "
                    + "u.id AS user_id, u.username, "
                    + "p.id AS pin_id, p.media_id, p.user_id AS pin_user_id, p.created_at AS pin_created_at "
                    + "FROM boards b "
                    + "JOIN users u ON b.user_id = u.id "
                    + "LEFT JOIN board_pin bp ON b.id = bp.board_id "
                    + "LEFT JOIN pins p ON p.id = bp.pin_id "
                    + "WHERE b.id = ?"),
            Mockito.any(BoardResultSetExtractor.class),
            Mockito.eq(1L));
  }

  @Test
  void findById_shouldThrowException_whenBoardDoesNotExists() {
    Mockito.when(
            jdbcTemplate.query(
                Mockito.anyString(), Mockito.any(BoardResultSetExtractor.class), Mockito.eq(1L)))
        .thenThrow(new EmptyResultDataAccessException(1));

    assertThrows(RuntimeException.class, () -> boardDao.findById(1L));
  }

  @Test
  void findAllByUserId_shouldReturnListOfBoard() {
    Mockito.when(
            jdbcTemplate.query(
                Mockito.eq(
                    "SELECT b.id AS board_id, b.board_name, b.create_at, "
                        + "u.id AS user_id, u.username, "
                        + "p.id AS pin_id, p.media_id, p.user_id AS pin_user_id "
                        + "FROM boards b "
                        + "JOIN users u ON b.user_id = u.id "
                        + "LEFT JOIN board_pin bp ON b.id = bp.board_id "
                        + "LEFT JOIN pins p ON p.id = bp.pin_id "
                        + "WHERE b.user_id = ? limit ? offset ?"),
                Mockito.any(BoardResultSetExtractor.class),
                Mockito.eq(1L),
                Mockito.eq(10),
                Mockito.eq(0)))
        .thenReturn(List.of(board));

    var result = boardDao.findAllByUserId(1L, 10, 0);
    assertNotNull(result);
    assertEquals(List.of(board), result);

    Mockito.verify(jdbcTemplate)
        .query(
            Mockito.eq(
                "SELECT b.id AS board_id, b.board_name, b.create_at, "
                    + "u.id AS user_id, u.username, "
                    + "p.id AS pin_id, p.media_id, p.user_id AS pin_user_id "
                    + "FROM boards b "
                    + "JOIN users u ON b.user_id = u.id "
                    + "LEFT JOIN board_pin bp ON b.id = bp.board_id "
                    + "LEFT JOIN pins p ON p.id = bp.pin_id "
                    + "WHERE b.user_id = ? limit ? offset ?"),
            Mockito.any(BoardResultSetExtractor.class),
            Mockito.eq(1L),
            Mockito.eq(10),
            Mockito.eq(0));
  }

  @Test
  void deleteById_shouldDeleteBoard_whenBoardExists() {
    Mockito.when(jdbcTemplate.update(Mockito.eq("DELETE FROM boards WHERE id = ?"), Mockito.eq(1L)))
        .thenReturn(1);

    var result = boardDao.deleteById(1L);
    assertEquals(1, result);
  }

  @Test
  void deleteById_shouldThrowException_whenPinDoesNotExists() {
    Mockito.when(jdbcTemplate.update(Mockito.anyString(), Mockito.eq(1L)))
        .thenThrow(new EmptyResultDataAccessException(1));
    assertThrows(BoardNotFoundException.class, () -> boardDao.deleteById(1L));
  }
}
