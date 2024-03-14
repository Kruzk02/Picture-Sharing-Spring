package com.app.DAO.Impl;

import com.app.DAO.BoardDao;
import com.app.Model.Board;
import com.app.Model.Pin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Implementation of BoardDao using Spring JDBC for data access.
 */
@Repository
public class BoardDaoImpl implements BoardDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BoardDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Board save(Board board,Long pinId) {
        try{
            String sql = "INSERT INTO boards (user_id,board_name) VALUES (?,?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int row = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1,board.getUser().getId());
                ps.setString(2,board.getName());
                return ps;
            },keyHolder);

            if(row >0){
                board.setId(keyHolder.getKey().longValue());
                Number lastInsertedId = keyHolder.getKey();

                String boardPinSql = "INSERT INTO Board_Pin (board_id,pin_id) VALUES (?,?)";
                jdbcTemplate.update(boardPinSql,lastInsertedId,pinId);
                return board;
            }else{
                return null;
            }
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public Board findById(Long id) {
        try{
            String sql = "SELECT b.id AS board_id, b.board_name, p.id AS pin_id, p.file_name, p.image_url, p.description " +
                    "FROM boards b " +
                    "JOIN board_pin bp ON b.id = bp.board_id " +
                    "JOIN pins p ON bp.pin_id = p.id " +
                    "WHERE b.id = ?";

            return jdbcTemplate.queryForObject(sql,new BoardRowMapper(),id);
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public int deleteById(Long id) {
        try{
            String sql = "DELETE FROM boards WHERE id = ?";
            return jdbcTemplate.update(sql,id);
        }catch (Exception e){
            return 0;
        }
    }
}

/**
 * RowMapper Implementation to map ResultSet row to Board object.
 */
class BoardRowMapper implements RowMapper<Board>{
    @Override
    public Board mapRow(ResultSet rs, int rowNum) throws SQLException {
        Board board = new Board();
        board.setId(rs.getLong("board_id"));
        board.setName(rs.getString("board_name"));

        long pinId = rs.getLong("pin_id");
        if (pinId > 0) {
            Pin pin = new Pin();
            pin.setId(pinId);
            pin.setImage_url(rs.getString("image_url"));
            pin.setDescription(rs.getString("description"));
            board.getPins().add(pin);
        }
        return board;
    }
}
