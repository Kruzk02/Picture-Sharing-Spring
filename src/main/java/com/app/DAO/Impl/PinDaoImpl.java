package com.app.DAO.Impl;

import com.app.DAO.PinDao;
import com.app.Model.Pin;
import com.app.exception.sub.PinNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

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

    @Override
    public List<Pin> getAllPins() {
        String sql = "SELECT * FROM pins";
        return jdbcTemplate.query(sql,new PinRowMapper());
    }

    @Override
    public Pin save(Pin pin) {
        try {
            String sql = "INSERT INTO pins(user_id,file_name, image_url, description) VALUES (?,?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int row = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1,pin.getUser().getId());
                ps.setString(2,pin.getFileName());
                ps.setString(3,pin.getImage_url());
                ps.setString(4,pin.getDescription());
                return ps;
            },keyHolder);
            if(row > 0){
                pin.setId(keyHolder.getKey().longValue());
                return pin;
            }else{
                return null;
            }
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public Pin findById(Long id) {
        try{
            String sql = "SELECT id, file_name, image_url, description FROM pins where id = ?";
            return jdbcTemplate.queryForObject(sql,new PinRowMapper(),id);
        }catch (DataAccessException e){
           throw new PinNotFoundException("Pin not found with a id: " + id);
        }
    }

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

/**
 * RowMapper Implementation to map ResultSet row to Pin object.
 */
class PinRowMapper implements RowMapper<Pin>{

    @Override
    public Pin mapRow(ResultSet rs, int rowNum) throws SQLException {
        Pin pin = new Pin();
        pin.setId(rs.getLong("id"));
        pin.setImage_url(rs.getString("image_url"));
        pin.setFileName(rs.getString("file_name"));
        pin.setDescription(rs.getString("description"));
        return pin;
    }
}