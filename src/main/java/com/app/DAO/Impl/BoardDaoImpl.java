package com.app.DAO.Impl;

import com.app.DAO.BoardDao;
import com.app.DAO.PinDao;
import com.app.DAO.UserDao;
import com.app.Model.Board;
import com.app.Model.Pin;
import com.app.Model.User;
import com.app.exception.sub.BoardNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of BoardDao using Spring JDBC for data access.
 */
@Repository
@AllArgsConstructor
public class BoardDaoImpl implements BoardDao {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Board save(Board board) {
        String sql = "INSERT INTO boards (user_id, board_name) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            int row = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, board.getUser().getId());
                ps.setString(2, board.getName());
                return ps;
            }, keyHolder);

            if (row > 0) {
                board.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
                Number lastInsertedId = keyHolder.getKey();

                String boardPinSql = "INSERT INTO board_pin (board_id, pin_id) VALUES (?, ?)";
                board.getPins().forEach(pin -> jdbcTemplate.update(boardPinSql, lastInsertedId, pin.getId()));

                return board;
            } else {
                throw new RuntimeException("Failed to insert board");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error saving board", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @Override
    public Board update(Board board, long id) {
        StringBuilder sb = new StringBuilder("UPDATE boards SET ");
        List<Object> params = new ArrayList<>();

        if (board.getName() != null) {
            sb.append("board_name = ?, ");
            params.add(board.getName());
        }

        if (board.getPins() != null) {
            String sql = "UPDATE board_pin SET pin_id = ? WHERE board_id = ?";
            board.getPins().forEach(pin -> jdbcTemplate.update(sql, id, pin.getId()));
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
        return rowAffected > 0 ? board : null;
    }

    @Override
    public Board findById(Long id) {
        try{
            String sql = "SELECT b.id AS board_id, b.board_name, b.create_at, " +
                    "u.id AS user_id, u.username," +
                    "p.id AS pin_id, p.description, p.media_id " +
                    "FROM boards b " +
                    "JOIN users u ON b.user_id = u.id " +
                    "LEFT JOIN board_pin bp ON b.id = bp.board_id " +
                    "LEFT JOIN pins p ON p.id = bp.pin_id " +
                    "WHERE b.id = ?";

            return jdbcTemplate.queryForObject(sql,new BoardRowMapper(), id);
        }catch (EmptyResultDataAccessException e){
            System.out.println(e.getCause() + e.getMessage());
            throw new BoardNotFoundException("Board not found with a id: " + id);
        }
    }

    @Override
    public List<Board> findAllByUserId(Long userId) {
        String sql = "SELECT b.id AS board_id, b.board_name, b.create_at, " +
                "u.id AS user_id, u.username," +
                "p.id AS pin_id, p.description, p.media_id " +
                "FROM boards b " +
                "JOIN users u ON b.user_id = u.id " +
                "LEFT JOIN board_pin bp ON b.id = bp.board_id " +
                "LEFT JOIN pins p ON p.id = bp.pin_id " +
                "WHERE b.user_id = ?";
        return jdbcTemplate.query(sql, new BoardResultSetExtractor(),userId);
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
class BoardRowMapper implements RowMapper<Board> {

    @Override
    public Board mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<Long, Board> boardMap = new HashMap<>();
        Long boardId = rs.getLong("board_id");

        while (rs.next()) {

            Board board = boardMap.get(boardId);
            if (board == null) {
                board = Board.builder()
                        .id(boardId)
                        .name(rs.getString("board_name"))
                        .build();

                User user = User.builder()
                        .id(rs.getLong("user_id"))
                        .username(rs.getString("username"))
                        .build();
                board.setUser(user);

                board.setPins(new ArrayList<>());
                boardMap.put(boardId, board);
            }

            long pinId = rs.getLong("pin_id");
            if (pinId != 0) {
                Pin pin = Pin.builder()
                        .id(pinId)
                        .description(rs.getString("description"))
                        .mediaId(rs.getLong("media_id"))
                        .build();
                board.getPins().add(pin);
            }
        }

        return boardMap.get(boardId);
    }
}

class BoardResultSetExtractor implements ResultSetExtractor<List<Board>> {

    @Override
    public List<Board> extractData(ResultSet rs) throws SQLException {
        Map<Long, Board> boardMap = new HashMap<>();

        while (rs.next()) {
            Long boardId = rs.getLong("board_id");

            Board board = boardMap.get(boardId);
            if (board == null) {
                board = Board.builder()
                        .id(boardId)
                        .name(rs.getString("board_name"))
                        .build();

                User user = User.builder()
                        .id(rs.getLong("user_id"))
                        .username(rs.getString("username"))
                        .build();
                board.setUser(user);

                board.setPins(new ArrayList<>());
                boardMap.put(boardId, board);
            }

            long pinId = rs.getLong("pin_id");
            if (pinId != 0) {
                Pin pin = Pin.builder()
                        .id(pinId)
                        .description(rs.getString("description"))
                        .mediaId(rs.getLong("media_id"))
                        .build();
                board.getPins().add(pin);
            }
        }

        return new ArrayList<>(boardMap.values());
    }
}
