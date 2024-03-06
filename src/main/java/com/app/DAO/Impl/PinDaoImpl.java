package com.app.DAO.Impl;

import com.app.DAO.PinDao;
import com.app.Model.Pin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class PinDaoImpl implements PinDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PinDaoImpl(JdbcTemplate jdbcTemplate) throws IOException {
        this.jdbcTemplate = jdbcTemplate;

        createDirectory();
    }

    @Override
    public List<Pin> getAllPins() {
        try{
            String sql = "SELECT * FROM pins";
            return jdbcTemplate.query(sql,new PinRowMapper());
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public Pin save(Pin pin) {
        try{
            String sql = "INSERT INTO pins(user_id,image_url,description) VALUES (?,?,?,?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int row = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1,pin.getUser().getId());
                ps.setString(2,pin.getImage_url());
                ps.setString(3,pin.getDescription());
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
            String sql = "SELECT * FROM pins where id = ?";
            return jdbcTemplate.queryForObject(sql,new PinRowMapper(),id);
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public int deleteById(Long id) {
        try{
            String sql = "DELETE FROM pins WHERE id = ?";
            return jdbcTemplate.update(sql,id);
        }catch (Exception e){
            return 0;
        }
    }

    public void createDirectory () throws IOException {
        String fileName = "C:\\media";
        Path path = Paths.get(fileName);

        if(!Files.exists(path)){
            Files.createDirectories(path);
            System.out.println("Directory created");
        } else {
            System.out.println("Directory already exists");
        }
    }
}
class PinRowMapper implements RowMapper<Pin>{

    @Override
    public Pin mapRow(ResultSet rs, int rowNum) throws SQLException {
        Pin pin = new Pin();
        pin.setId(rs.getLong("id"));
        pin.setImage_url(rs.getString("image_url"));
        pin.setDescription(rs.getString("description"));
        return pin;
    }
}