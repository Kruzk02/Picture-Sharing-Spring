package com.app.DAO.Impl;

import com.app.DAO.PinDao;
import com.app.Model.Hashtag;
import com.app.Model.Pin;
import com.app.Model.SortType;
import com.app.exception.sub.PinNotFoundException;
import com.app.exception.sub.UserNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of PinDao using Spring JDBC for data access. */
@Repository
public class PinDaoImpl implements PinDao {

  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public PinDaoImpl(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public List<Pin> getAllPins(SortType sortType, int limit, int offset) {
    String orderBy = (sortType == SortType.NEWEST) ? "DESC" : "ASC";

    String sql =
        "SELECT id, user_id, media_id, created_at FROM pins ORDER BY created_at "
            + orderBy
            + " LIMIT ? OFFSET ?";

    return jdbcTemplate.query(sql, new PinRowMapper(false, true), limit, offset);
  }

  @Transactional(readOnly = true)
  @Override
  public List<Pin> getAllPinsByHashtag(String tag, int limit, int offset) {
    String sql =
        "SELECT p.id, p.user_id, p.media_id, p.created_at "
            + "FROM pins p "
            + "JOIN hashtags_pins hp ON p.id = hp.pin_id "
            + "JOIN hashtags h ON hp.hashtag_id = h.id "
            + "WHERE h.tag = ? ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
    return jdbcTemplate.query(sql, new PinRowMapper(false, true), tag, limit, offset);
  }

  @Override
  public Pin save(Pin pin) {
    try {
      String sql = "INSERT INTO pins(user_id, description, media_id) VALUES (?, ?, ?)";
      KeyHolder keyHolder = new GeneratedKeyHolder();

      int row =
          jdbcTemplate.update(
              con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, pin.getUserId());
                ps.setString(2, pin.getDescription());
                ps.setLong(3, pin.getMediaId());
                return ps;
              },
              keyHolder);
      if (row > 0) {
        pin.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        assignHashtagToPin(pin.getId(), pin.getHashtags());
        return pin;
      } else {
        return null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private void assignHashtagToPin(Long pinId, Collection<Hashtag> hashtags) {
    String sql = "INSERT INTO hashtags_pins(hashtag_id, pin_id) VALUES(?, ?)";
    List<Hashtag> tags = hashtags.stream().toList();

    jdbcTemplate.batchUpdate(
        sql,
        new BatchPreparedStatementSetter() {

          @Override
          public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setLong(1, tags.get(i).getId());
            ps.setLong(2, pinId);
          }

          @Override
          public int getBatchSize() {
            return tags.size();
          }
        });
  }

  @Override
  public Pin update(Long id, Pin pin) {
    StringBuilder sb = new StringBuilder("UPDATE pins SET ");
    List<Object> params = new ArrayList<>();

    if (pin.getDescription() != null) {
      sb.append("description = ?, ");
      params.add(pin.getDescription());
    }

    if (pin.getMediaId() != 0) {
      sb.append("media_id = ?, ");
      params.add(pin.getMediaId());
    }

    if (pin.getHashtags() != null && !pin.getHashtags().isEmpty()) {
      String sql = "DELETE FROM hashtags_pins WHERE pin_id = ?";
      jdbcTemplate.update(sql, pin.getId());

      assignHashtagToPin(pin.getId(), pin.getHashtags());
    }

    if (params.isEmpty()) {
      throw new IllegalArgumentException("No fields to update");
    }

    if (!sb.isEmpty()) {
      sb.setLength(sb.length() - 2);
    }

    sb.append(" WHERE id = ?");
    params.add(id);

    String sql = sb.toString();
    int rowAffected = jdbcTemplate.update(sql, params.toArray());
    return rowAffected > 0 ? pin : null;
  }

  @Override
  public Pin findById(Long id, boolean fetchDetails) {
    try {
      if (fetchDetails) {
        String sql =
            "SELECT p.id AS pin_id, p.user_id, p.description, p.media_id, p.created_at, "
                + "h.id AS hashtag_id, h.tag "
                + "FROM pins p "
                + "LEFT JOIN hashtags_pins hp ON hp.pin_id = p.id "
                + "LEFT JOIN hashtags h ON h.id = hp.hashtag_id "
                + "WHERE p.id = ?";
        return jdbcTemplate.query(sql, new PinRSE(), id);
      } else {
        String sql = "SELECT id, media_id, user_id, created_at FROM pins where id = ?";
        return jdbcTemplate.queryForObject(sql, new PinRowMapper(false, true), id);
      }
    } catch (DataAccessException e) {
      throw new PinNotFoundException("Pin not found with a id: " + id);
    }
  }

  @Override
  public List<Pin> findPinByUserId(Long userId, int limit, int offset) {
    try {
      String sql =
          "SELECT id, user_id, media_id, created_at FROM pins WHERE user_id = ? ORDER BY created_at DESC limit ? offset ?";
      return jdbcTemplate.query(sql, new PinRowMapper(false, true), userId, limit, offset);
    } catch (DataAccessException e) {
      throw new UserNotFoundException("User not found with a id: " + userId);
    }
  }

  @Override
  public int deleteById(Long id) {
    try {
      String sql = "DELETE FROM pins WHERE id = ?";
      return jdbcTemplate.update(sql, id);
    } catch (DataAccessException e) {
      throw new PinNotFoundException("Pin not found with a id: " + id);
    }
  }
}

class PinRSE implements ResultSetExtractor<Pin> {
  @Override
  public Pin extractData(ResultSet rs) throws SQLException {
    Pin pin = null;

    while (rs.next()) {
      if (pin == null) {
        pin = new Pin();
        pin.setId(rs.getLong("pin_id"));
        pin.setUserId(rs.getLong("user_id"));
        pin.setDescription(rs.getString("description"));
        pin.setMediaId(rs.getLong("media_id"));
        pin.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        pin.setHashtags(new ArrayList<>());
      }

      Long hashtagId = rs.getLong("hashtag_id");
      if (!rs.wasNull()) {
        Hashtag hashtag = new Hashtag();
        hashtag.setId(hashtagId);
        hashtag.setTag(rs.getString("tag"));
        pin.getHashtags().add(hashtag);
      }
    }

    return pin;
  }
}

class PinRowMapper implements RowMapper<Pin> {

  private final boolean includedDescription;
  private final boolean includedMediaId;

  PinRowMapper(boolean includedDescription, boolean includedMediaId) {
    this.includedDescription = includedDescription;
    this.includedMediaId = includedMediaId;
  }

  @Override
  public Pin mapRow(ResultSet rs, int rowNum) throws SQLException {

    Pin pin = new Pin();
    pin.setId(rs.getLong("id"));

    if (includedDescription) {
      pin.setDescription(rs.getString("description"));
    }

    if (includedMediaId) {
      pin.setMediaId(rs.getLong("media_id"));
    }

    pin.setUserId(rs.getLong("user_id"));
    pin.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    return pin;
  }
}
