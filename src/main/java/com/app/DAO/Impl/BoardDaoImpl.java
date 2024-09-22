package com.app.DAO.Impl;

import com.app.DAO.BoardDao;
import com.app.DAO.PinDao;
import com.app.DAO.UserDao;
import com.app.Model.Board;
import com.app.exception.sub.BoardNotFoundException;
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
import java.util.List;

/**
 * Implementation of BoardDao using Spring JDBC for data access.
 */
@Repository
public class BoardDaoImpl implements BoardDao {

    private final JdbcTemplate jdbcTemplate;
    private final UserDao userDao;
    private final PinDao pinDao;
    @Autowired
    public BoardDaoImpl(JdbcTemplate jdbcTemplate, UserDao userDao, PinDao pinDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDao = userDao;
        this.pinDao = pinDao;
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

                String boardPinSql = "INSERT INTO board_pin (board_id,pin_id) VALUES (?,?)";
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
            String sql = "SELECT b.id AS board_id, b.board_name,b.user_id, p.id AS pin_id, p.file_name, p.image_url, p.description " +
                    "FROM boards b " +
                    "JOIN board_pin bp ON b.id = bp.board_id " +
                    "JOIN pins p ON bp.pin_id = p.id " +
                    "WHERE b.id = ?";

            return jdbcTemplate.queryForObject(sql,new BoardRowMapper(pinDao,userDao),id);
        }catch (Exception e){
            System.out.println(e.getCause() + e.getMessage());
            throw new BoardNotFoundException("Board not found with a id: " + id);
        }
    }

    @Override
    public List<Board> findAllByUserId(Long userId) {
        String sql = "SELECT * from boards WHERE user_id = ?";
        return jdbcTemplate.query(sql,(rs, rowNum) -> {
            Board board = new Board();
            board.setId(rs.getLong("id"));
            board.setName(rs.getString("board_name"));
            board.setUser(userDao.findUserById(rs.getLong("user_id")));
            return board;
        },userId);
    }

    @Override
    public int deleteById(Long id) {
        try{
            String sql = "DELETE FROM boards WHERE id = ?";
            return jdbcTemplate.update(sql,id);
        }catch (Exception e){
           throw new BoardNotFoundException("Board not found with id:" + id);
        }
    }
}

/**
 * RowMapper Implementation to map ResultSet row to Board object.
 */
class BoardRowMapper implements RowMapper<Board>{
    private final PinDao pinDao;
    private final UserDao userDao;

    public BoardRowMapper(PinDao pinDao, UserDao userDao) {
        this.pinDao = pinDao;
        this.userDao = userDao;
    }

    @Override
    public Board mapRow(ResultSet rs, int rowNum) throws SQLException {
        Board board = new Board();
        board.setId(rs.getLong("board_id"));
        board.setName(rs.getString("board_name"));
        board.setPins(List.of(pinDao.findById(rs.getLong("pin_id"))));
        board.setUser(userDao.findUserById(rs.getLong("user_id")));
        return board;
    }
}
