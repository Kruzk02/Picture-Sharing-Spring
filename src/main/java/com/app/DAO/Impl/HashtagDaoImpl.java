package com.app.DAO.Impl;

import com.app.DAO.HashtagDao;
import com.app.Model.Hashtag;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

@AllArgsConstructor
@Repository
public class HashtagDaoImpl implements HashtagDao {

    private final JdbcTemplate template;

    @Override
    public Hashtag findByTag(String tag) {
        String sql = "select id, created_at FROM hashtags WHERE tag = ?";
        return template.queryForObject(sql, (rs, rowNum) ->
            Hashtag.builder()
                .id(rs.getLong("id"))
                .tag(tag)
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build()
        , tag);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @Override
    public Hashtag save(Hashtag hashtag) {
        String sql = "INSERT INTO hashtags(tag) VALUES(?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rows = template.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, hashtag.getTag());
            return ps;
        }, keyHolder);

        if (rows > 0) {
            hashtag.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
            return hashtag;
        } else {
            return null;
        }
    }
}
