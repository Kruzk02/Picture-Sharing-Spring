package com.app.DAO.Impl;

import static org.junit.jupiter.api.Assertions.*;

import com.app.Model.Hashtag;
import com.app.Model.Pin;
import com.app.Model.SortType;
import com.app.exception.sub.PinNotFoundException;
import com.app.exception.sub.UserNotFoundException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@ExtendWith(MockitoExtension.class)
class PinDaoImplTest {

  @InjectMocks private PinDaoImpl pinDao;
  @Mock private JdbcTemplate jdbcTemplate;

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
  }

  @Test
  void getAllPins_shouldReturnListOfPins() {
    Mockito.when(
            jdbcTemplate.query(
                Mockito.eq(
                    "SELECT id, user_id, media_id, created_at FROM pins ORDER BY created_at DESC LIMIT ? OFFSET ?"),
                Mockito.any(PinRowMapper.class),
                Mockito.eq(10),
                Mockito.eq(0)))
        .thenReturn(List.of(pin));

    var result = pinDao.getAllPins(SortType.NEWEST, 10, 0);

    assertNotNull(result);
    assertEquals(List.of(pin), result);

    Mockito.verify(jdbcTemplate)
        .query(
            Mockito.eq(
                "SELECT id, user_id, media_id, created_at FROM pins ORDER BY created_at DESC LIMIT ? OFFSET ?"),
            Mockito.any(PinRowMapper.class),
            Mockito.eq(10),
            Mockito.eq(0));
  }

  @Test
  void getAllPinsByHashtag_shouldReturnListOfPins() {
    Mockito.when(
            jdbcTemplate.query(
                Mockito.eq(
                    "SELECT p.id, p.user_id, p.media_id, p.created_at "
                        + "FROM pins p "
                        + "JOIN hashtags_pins hp ON p.id = hp.pin_id "
                        + "JOIN hashtags h ON hp.hashtag_id = h.id "
                        + "WHERE h.tag = ? ORDER BY p.created_at DESC LIMIT ? OFFSET ?"),
                Mockito.any(PinRowMapper.class),
                Mockito.eq("tag"),
                Mockito.eq(10),
                Mockito.eq(0)))
        .thenReturn(List.of(pin));

    var result = pinDao.getAllPinsByHashtag("tag", 10, 0);

    assertNotNull(result);
    assertEquals(List.of(pin), result);

    Mockito.verify(jdbcTemplate)
        .query(
            Mockito.eq(
                "SELECT p.id, p.user_id, p.media_id, p.created_at "
                    + "FROM pins p "
                    + "JOIN hashtags_pins hp ON p.id = hp.pin_id "
                    + "JOIN hashtags h ON hp.hashtag_id = h.id "
                    + "WHERE h.tag = ? ORDER BY p.created_at DESC LIMIT ? OFFSET ?"),
            Mockito.any(PinRowMapper.class),
            Mockito.eq("tag"),
            Mockito.eq(10),
            Mockito.eq(0));
  }

  @Test
  void save_shouldInsertPinAndAssignHashTag() {
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

    var result = pinDao.save(pin);

    assertNotNull(result);
    assertEquals(pin.getId(), result.getId());

    Mockito.verify(jdbcTemplate)
        .batchUpdate(
            Mockito.eq("INSERT INTO hashtags_pins(hashtag_id, pin_id) VALUES(?, ?)"),
            Mockito.any(BatchPreparedStatementSetter.class));
  }

  @Test
  void save_shouldReturnNull_whenInsertFail() {
    Mockito.when(
            jdbcTemplate.update(
                Mockito.any(PreparedStatementCreator.class), Mockito.any(KeyHolder.class)))
        .thenReturn(0);

    var result = pinDao.save(pin);

    assertNull(result);
    Mockito.verify(jdbcTemplate, Mockito.never())
        .batchUpdate(Mockito.anyString(), Mockito.any(BatchPreparedStatementSetter.class));
  }

  @Test
  void findById_shouldReturnPin_whenPinExists() {
    Mockito.when(
            jdbcTemplate.queryForObject(
                Mockito.eq("SELECT id, media_id, user_id, created_at FROM pins where id = ?"),
                Mockito.any(PinRowMapper.class),
                Mockito.eq(1L)))
        .thenReturn(pin);

    var result = pinDao.findById(1L, false);

    assertNotNull(result);
    assertEquals(pin, result);

    Mockito.verify(jdbcTemplate)
        .queryForObject(
            Mockito.eq("SELECT id, media_id, user_id, created_at FROM pins where id = ?"),
            Mockito.any(PinRowMapper.class),
            Mockito.eq(1L));
  }

  @Test
  void findById_shouldThrowException_whenPinDoesNotExists() {
    Mockito.when(
            jdbcTemplate.queryForObject(
                Mockito.anyString(), Mockito.any(PinRowMapper.class), Mockito.eq(1L)))
        .thenThrow(new EmptyResultDataAccessException(1));
    assertThrows(PinNotFoundException.class, () -> pinDao.findById(1L, false));
  }

  @Test
  void findById_shouldReturnPinWithDetails_whenPinExists() {
    Mockito.when(
            jdbcTemplate.query(
                Mockito.eq(
                    "SELECT p.id AS pin_id, p.user_id, p.description, p.media_id, p.created_at, "
                        + "h.id AS hashtag_id, h.tag "
                        + "FROM pins p "
                        + "LEFT JOIN hashtags_pins hp ON hp.pin_id = p.id "
                        + "LEFT JOIN hashtags h ON h.id = hp.hashtag_id "
                        + "WHERE p.id = ?"),
                Mockito.any(PinRSE.class),
                Mockito.eq(1L)))
        .thenReturn(pin);
    var result = pinDao.findById(1L, true);

    assertNotNull(result);
    assertEquals(pin, result);

    Mockito.verify(jdbcTemplate)
        .query(
            Mockito.eq(
                "SELECT p.id AS pin_id, p.user_id, p.description, p.media_id, p.created_at, "
                    + "h.id AS hashtag_id, h.tag "
                    + "FROM pins p "
                    + "LEFT JOIN hashtags_pins hp ON hp.pin_id = p.id "
                    + "LEFT JOIN hashtags h ON h.id = hp.hashtag_id "
                    + "WHERE p.id = ?"),
            Mockito.any(PinRSE.class),
            Mockito.eq(1L));
  }

  @Test
  void findById_shouldThrowException_whenPinWithDetailsDoesNotExists() {
    Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(PinRSE.class), Mockito.eq(1L)))
        .thenThrow(new EmptyResultDataAccessException(1));
    assertThrows(PinNotFoundException.class, () -> pinDao.findById(1L, true));
  }

  @Test
  void findPinByUserId_shouldReturnListOfPin_whenUserExists() {
    Mockito.when(
            jdbcTemplate.query(
                Mockito.eq(
                    "SELECT id, user_id, media_id, created_at FROM pins WHERE user_id = ? ORDER BY created_at DESC limit ? offset ?"),
                Mockito.any(PinRowMapper.class),
                Mockito.eq(1L),
                Mockito.eq(10),
                Mockito.eq(0)))
        .thenReturn(List.of(pin));

    var result = pinDao.findPinByUserId(1L, 10, 0);

    assertNotNull(result);
    assertEquals(List.of(pin), result);

    Mockito.verify(jdbcTemplate)
        .query(
            Mockito.eq(
                "SELECT id, user_id, media_id, created_at FROM pins WHERE user_id = ? ORDER BY created_at DESC limit ? offset ?"),
            Mockito.any(PinRowMapper.class),
            Mockito.eq(1L),
            Mockito.eq(10),
            Mockito.eq(0));
  }

  @Test
  void findPinByUserId_shouldThrowException_whenUserDoesNotExists() {
    Mockito.when(
            jdbcTemplate.query(
                Mockito.anyString(),
                Mockito.any(PinRowMapper.class),
                Mockito.eq(1L),
                Mockito.eq(10),
                Mockito.eq(0)))
        .thenThrow(new EmptyResultDataAccessException(1));
    assertThrows(UserNotFoundException.class, () -> pinDao.findPinByUserId(1L, 10, 0));
  }

  @Test
  void deleteById_shouldDeletePin_whenPinExists() {
    Mockito.when(jdbcTemplate.update(Mockito.eq("DELETE FROM pins WHERE id = ?"), Mockito.eq(1L)))
        .thenReturn(1);

    var result = pinDao.deleteById(1L);

    assertEquals(1L, result);
  }

  @Test
  void deleteById_shouldThrowException_whenPinDoesNotExists() {
    Mockito.when(jdbcTemplate.update(Mockito.anyString(), Mockito.eq(1L)))
        .thenThrow(new EmptyResultDataAccessException(1));
    assertThrows(PinNotFoundException.class, () -> pinDao.deleteById(1L));
  }
}
