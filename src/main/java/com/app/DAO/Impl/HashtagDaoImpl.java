package com.app.DAO.Impl;

import com.app.DAO.HashtagDao;
import com.app.Model.Hashtag;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class HashtagDaoImpl implements HashtagDao {

  private final JdbcTemplate template;

  @Override
  public Map<String, Hashtag> findByTag(Set<String> tag) {
    if (tag == null || tag.isEmpty()) {
      return Collections.emptyMap();
    }

    String sql = "SELECT id, tag, created_at FROM hashtags WHERE tag IN (:tags)";
    NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(template);

    Map<String, Object> params = Map.of("tags", tag);

    List<Hashtag> foundItems =
        namedJdbcTemplate.query(
            sql,
            params,
            (rs, rowNum) ->
                Hashtag.builder()
                    .id(rs.getLong("id"))
                    .tag(rs.getString("tag"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .build());

    return foundItems.stream()
        .collect(
            Collectors.toMap(
                Hashtag::getTag, hashtag -> hashtag, (existing, replacement) -> existing));
  }

  @Override
  public Hashtag save(Hashtag hashtag) {
    String sql = "INSERT INTO hashtags(tag) VALUES(?)";
    KeyHolder keyHolder = new GeneratedKeyHolder();

    int rows =
        template.update(
            conn -> {
              PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
              ps.setString(1, hashtag.getTag());
              return ps;
            },
            keyHolder);

    if (rows > 0) {
      hashtag.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
      return hashtag;
    } else {
      return null;
    }
  }
}
