package com.app.DAO.Impl;

import com.app.DAO.BoardDao;
import com.app.Model.Board;
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

@Repository
public class BoardDaoImpl implements BoardDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BoardDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Board save(Board board) {
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
            String sql = "SELECT * from boards where id = ?";
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

class BoardRowMapper implements RowMapper<Board>{
    @Override
    public Board mapRow(ResultSet rs, int rowNum) throws SQLException {
        Board board = new Board();
        board.setId(rs.getLong("id"));
        board.setName(rs.getString("board_name"));
        return board;
    }
}
