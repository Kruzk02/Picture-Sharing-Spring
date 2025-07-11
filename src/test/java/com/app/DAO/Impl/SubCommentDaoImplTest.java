package com.app.DAO.Impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.Model.*;
import com.app.exception.sub.SubCommentNotFoundException;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@ExtendWith(MockitoExtension.class)
class SubCommentDaoImplTest {

  @InjectMocks private SubCommentDaoImpl subCommentDao;
  @Mock private JdbcTemplate jdbcTemplate;

  private SubComment subComment;

  @BeforeEach
  void setUp() {
    subComment =
        SubComment.builder()
            .id(1L)
            .content("content")
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
            .comment(
                Comment.builder()
                    .id(1L)
                    .content("content")
                    .pinId(1L)
                    .userId(1L)
                    .hashtags(List.of(Hashtag.builder().id(1L).tag("tag").build()))
                    .mediaId(1L)
                    .build())
            .media(Media.builder().id(1L).mediaType(MediaType.IMAGE).url("NO").build())
            .build();
  }

  @Test
  void save_shouldInsertSubComment() {
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    keyHolder.getKeyList().add(Map.of("GENERATED_KEY", 1L));

    when(jdbcTemplate.update(
            Mockito.any(PreparedStatementCreator.class), Mockito.any(KeyHolder.class)))
        .thenAnswer(
            invocation -> {
              KeyHolder kh = invocation.getArgument(1);
              kh.getKeyList().add(Map.of("GENERATED_KEY", 1L));
              return 1;
            });

    var result = subCommentDao.save(subComment);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("content", result.getContent());
  }

  @Test
  void findAllByCommentId_shouldReturnListOfSubComment_whenCommentExists() {
    when(jdbcTemplate.query(
            eq(
                "SELECT sc.id AS sc_id, sc.content AS sc_content, sc.create_at as sc_create_at, "
                    + "sc.media_id AS sc_media_id, "
                    + "sc.comment_id AS sc_comment_id, "
                    + "u.id AS user_id, "
                    + "u.username AS user_username, "
                    + "c.content AS comment_content "
                    + "FROM sub_comments sc "
                    + "JOIN users u ON sc.user_id = u.id "
                    + "JOIN comments c ON sc.comment_id = c.id "
                    + "WHERE sc.comment_id = ? ORDER BY sc.create_at DESC limit ? offset ?"),
            any(SubCommentRowMapper.class),
            eq(1L),
            eq(5),
            eq(10)))
        .thenReturn(List.of(subComment));

    var result = subCommentDao.findAllByCommentId(1L, SortType.NEWEST, 5, 10);
    assertNotNull(result);
    assertEquals(List.of(subComment), result);

    verify(jdbcTemplate)
        .query(
            eq(
                "SELECT sc.id AS sc_id, sc.content AS sc_content, sc.create_at as sc_create_at, "
                    + "sc.media_id AS sc_media_id, "
                    + "sc.comment_id AS sc_comment_id, "
                    + "u.id AS user_id, "
                    + "u.username AS user_username, "
                    + "c.content AS comment_content "
                    + "FROM sub_comments sc "
                    + "JOIN users u ON sc.user_id = u.id "
                    + "JOIN comments c ON sc.comment_id = c.id "
                    + "WHERE sc.comment_id = ? ORDER BY sc.create_at DESC limit ? offset ?"),
            any(SubCommentRowMapper.class),
            eq(1L),
            eq(5),
            eq(10));
  }

  @Test
  void findById_shouldReturnSubComment_whenSubCommentExists() {
    when(jdbcTemplate.queryForObject(
            eq(
                "SELECT sc.id as sc_id, sc.content AS sc_content, sc.create_at AS sc_create_at, "
                    + "sc.media_id AS sc_media_id, "
                    + "sc.comment_id AS sc_comment_id, "
                    + "u.id AS user_id, "
                    + "u.username AS user_username, "
                    + "c.content AS comment_content "
                    + "FROM sub_comments sc "
                    + "JOIN users u ON sc.user_id = u.id "
                    + "JOIN comments c ON sc.comment_id = c.id "
                    + "WHERE sc.id = ?"),
            any(SubCommentRowMapper.class),
            eq(1L)))
        .thenReturn(subComment);

    var result = subCommentDao.findById(1L);
    assertNotNull(result);
    assertEquals(subComment, result);

    verify(jdbcTemplate)
        .queryForObject(
            eq(
                "SELECT sc.id as sc_id, sc.content AS sc_content, sc.create_at AS sc_create_at, "
                    + "sc.media_id AS sc_media_id, "
                    + "sc.comment_id AS sc_comment_id, "
                    + "u.id AS user_id, "
                    + "u.username AS user_username, "
                    + "c.content AS comment_content "
                    + "FROM sub_comments sc "
                    + "JOIN users u ON sc.user_id = u.id "
                    + "JOIN comments c ON sc.comment_id = c.id "
                    + "WHERE sc.id = ?"),
            any(SubCommentRowMapper.class),
            eq(1L));
  }

  @Test
  void findById_shouldThrowException_whenSubCommentDoesNotExists() {
    when(jdbcTemplate.queryForObject(anyString(), any(SubCommentRowMapper.class), eq(1L)))
        .thenThrow(new EmptyResultDataAccessException(1));
    assertThrows(SubCommentNotFoundException.class, () -> subCommentDao.findById(1L));
  }

  @Test
  void deleteById_shouldReturnUpdateCount_whenSuccess() {
    when(jdbcTemplate.update(eq("DELETE FROM sub_comments WHERE id = ?"), eq(1L))).thenReturn(1);

    var result = subCommentDao.deleteById(1L);

    assertEquals(1, result);
    verify(jdbcTemplate).update(eq("DELETE FROM sub_comments WHERE id = ?"), eq(1L));
  }

  @Test
  void deleteById_shouldThrowSubCommentNotFoundException_whenDataAccessExceptionOccurs() {
    when(jdbcTemplate.update(anyString(), eq(1L))).thenThrow(new EmptyResultDataAccessException(1));
    assertThrows(SubCommentNotFoundException.class, () -> subCommentDao.deleteById(1L));
  }
}
