package com.app.DAO.Impl;

import com.app.DAO.PinDao;
import com.app.Model.Pin;
import com.app.exception.sub.PinNotFoundException;
import com.app.exception.sub.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of PinDao using Spring JDBC for data access.
 */
@Repository
public class PinDaoImpl implements PinDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PinDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Pin> getAllPins(int offset) {
        String sql = "SELECT id, user_id, media_id, created_at FROM pins LIMIT 5 OFFSET ?";
        return jdbcTemplate.query(sql, new PinRowMapper(false, true), offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Pin save(Pin pin) {
        try {
            String sql = "INSERT INTO pins(user_id, description, media_id) VALUES (?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int row = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1,pin.getUserId());
                ps.setString(2,pin.getDescription());
                ps.setLong(3, pin.getMediaId());
                return ps;
            },keyHolder);
            if(row > 0){
                pin.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
                return pin;
            }else{
                return null;
            }
        }catch (Exception e){
            return null;
        }
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

    @Transactional(readOnly = true)
    @Override
    public Pin findBasicById(Long id) {
        try{
            String sql = "SELECT id, media_id, user_id, created_at FROM pins where id = ?";
            return jdbcTemplate.queryForObject(sql, new PinRowMapper(false, true),id);
        }catch (DataAccessException e){
            throw new PinNotFoundException("Pin not found with a id: " + id);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Pin findFullById(Long id) {
        try{
            String sql = "SELECT * FROM pins where id = ?";
            return jdbcTemplate.queryForObject(sql, new PinRowMapper(true, true),id);
        }catch (DataAccessException e){
            throw new PinNotFoundException("Pin not found with a id: " + id);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Pin> findNewestPin(int limit, int offset) {
        try {
            String sql = "SELECT id, user_id, media_id, created_at FROM pins ORDER BY created_at DESC limit ? offset ?";
            return jdbcTemplate.query(sql, new PinRowMapper(false, true), limit, offset);
        } catch (DataAccessException e) {
            throw new PinNotFoundException("Pin not found");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Pin> findOldestPin(int limit, int offset) {
        try {
            String sql = "SELECT id, user_id, media_id, created_at FROM pins ORDER BY created_at ASC limit ? offset ?";
            return jdbcTemplate.query(sql, new PinRowMapper(false, true), limit, offset);
        } catch (DataAccessException e) {
            throw new PinNotFoundException("Pin not found");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Pin> findPinByUserId(Long userId, int limit, int offset) {
        try {
            String sql = "SELECT id, user_id, media_id, created_at FROM pins WHERE user_id = ? ORDER BY created_at DESC limit ? offset ?\n";
            return jdbcTemplate.query(sql, new PinRowMapper(false, true), userId, limit, offset);
        } catch (DataAccessException e) {
            throw new UserNotFoundException("User not found with a id: " + userId);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Pin findUserIdByPinId(Long pinId) {
        try {
            String sql = "SELECT id, user_id, created_at FROM pins where id = ?";
            return jdbcTemplate.queryForObject(sql, new PinRowMapper(false, false),pinId);
        } catch (DataAccessException e) {
            throw new PinNotFoundException("Pin not found with a id: " + pinId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @Override
    public int deleteById(Long id) {
        try{
            String sql = "DELETE FROM pins WHERE id = ?";
            return jdbcTemplate.update(sql,id);
        }catch (DataAccessException e){
            throw new PinNotFoundException("Pin not found with a id: " + id);
        }
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
        pin.setCreatedAt(rs.getTimestamp("created_at"));
        return pin;
    }
}