package com.app.DAO.Impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.app.Model.Hashtag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@ExtendWith(MockitoExtension.class)
class HashtagDaoImplTest {

  @InjectMocks private HashtagDaoImpl hashtagDao;
  @Mock private JdbcTemplate jdbcTemplate;

  private Hashtag hashtag;

  @BeforeEach
  void setUp() {
    hashtag = Hashtag.builder().id(1L).tag("tag").build();
  }

  @Test
  void testFindByTag_returnsMapOfHashtags() {
    Set<String> inputTags = Set.of("java", "spring");

    List<Hashtag> mockResult =
        List.of(
            Hashtag.builder().id(1L).tag("java").createdAt(LocalDateTime.now()).build(),
            Hashtag.builder().id(2L).tag("spring").createdAt(LocalDateTime.now()).build());

    try (MockedConstruction<NamedParameterJdbcTemplate> mockConstruction =
        Mockito.mockConstruction(
            NamedParameterJdbcTemplate.class,
            (mock, context) ->
                when(mock.query(
                        eq("SELECT id, tag, created_at FROM hashtags WHERE tag IN (:tags)"),
                        any(Map.class),
                        any(RowMapper.class)))
                    .thenReturn(mockResult))) {

      Map<String, Hashtag> result = hashtagDao.findByTag(inputTags);

      assertEquals(2, result.size());
      assertTrue(result.containsKey("java"));
      assertTrue(result.containsKey("spring"));
    }
  }

  @Test
  void save_shouldInsertHashTag() {
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    keyHolder.getKeyList().add(Map.of("GENERATED_KEY", 1L));

    when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
        .thenAnswer(
            invocation -> {
              KeyHolder kh = invocation.getArgument(1);
              kh.getKeyList().add(Map.of("GENERATED_KEY", 1L));
              return 1;
            });

    var result = hashtagDao.save(hashtag);
    assertNotNull(result);
    assertEquals(hashtag, result);
  }

  @Test
  void save_shouldReturnNull_whenInsertFails() {
    when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
        .thenReturn(0);

    var result = hashtagDao.save(hashtag);
    assertNull(result);
  }
}
