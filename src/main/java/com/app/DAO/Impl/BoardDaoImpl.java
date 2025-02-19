package com.app.DAO.Impl;

import com.app.DAO.BoardDao;
import com.app.Model.Board;
import com.app.Model.Pin;
import com.app.Model.User;
import com.app.exception.sub.BoardNotFoundException;
import lombok.AllArgsConstructor;
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
                Number lastInsertedId = keyHolder.getKey();
                if (lastInsertedId == null) {
                    throw new RuntimeException("Failed to retrieve generated board ID");
                }

                board.setId(keyHolder.getKey().longValue());

                if (board.getPins() != null && !board.getPins().isEmpty()) {
                    String boardPinSql = "INSERT INTO board_pin (board_id, pin_id) VALUES (?, ?)";
                    jdbcTemplate.batchUpdate(boardPinSql, board.getPins(), board.getPins().size(), (ps, pin) -> {
                        ps.setLong(1, board.getId());
                        ps.setLong(2, pin.getId());
                    });
                }

                return board;
            } else {
                throw new RuntimeException("Failed to insert board");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error saving board", e);
        }
    }

    @Override
    @Transactional
    public Board addPinToBoard(Pin pin, Board board) {
        String sql = "INSERT INTO board_pin (board_id, pin_id) VALUES(?,?)";
        int rowAffected = jdbcTemplate.update(sql, board.getId(), pin.getId());

        if (rowAffected > 0) {
            board.getPins().add(pin);
            return board;
        }
        return null;
    }

    @Override
    @Transactional
    public Board deletePinFromBoard(Pin pin, Board board) {
        String sql = "DELETE FROM board_pin WHERE board_id AND pin_id = ?";
        int rowAffected = jdbcTemplate.update(sql, board.getId(), pin.getId());
        if (rowAffected > 0) {
            board.getPins().remove(pin);
            return board;
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    @Override
    public Board update(Board board, long id) {
        StringBuilder sb = new StringBuilder("UPDATE boards SET ");
        List<Object> params = new ArrayList<>();

        if (board.getName() != null) {
            sb.append("board_name = ?, ");
            params.add(board.getName());
        }

        if (params.isEmpty()) {
            throw new IllegalArgumentException("No fields to update");
        }

        sb.setLength(sb.length() - 2);
        sb.append(" WHERE id = ?");
        params.add(id);

        int rowAffected = jdbcTemplate.update(sb.toString(), params.toArray());

        return rowAffected > 0 ? board : null;
    }

    @Override
    public Board findById(Long id) {
        try {
            String sql = "SELECT b.id AS board_id, b.board_name, b.create_at, " +
                    "u.id AS user_id, u.username, " +
                    "p.id AS pin_id, p.media_id, p.user_id AS pin_user_id " +
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
    public List<Board> findAllByUserId(Long userId, int limit, int offset) {
        String sql = "SELECT b.id AS board_id, b.board_name, b.create_at, " +
                "u.id AS user_id, u.username, " +
                "p.id AS pin_id, p.media_id, p.user_id AS pin_user_id " +
                "FROM boards b " +
                "JOIN users u ON b.user_id = u.id " +
                "LEFT JOIN board_pin bp ON b.id = bp.board_id " +
                "LEFT JOIN pins p ON p.id = bp.pin_id " +
                "WHERE b.user_id = ? limit ? offset ?";
        return jdbcTemplate.query(sql, new BoardResultSetExtractor(),userId, limit, offset);
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
                        .mediaId(rs.getLong("media_id"))
                        .userId(rs.getLong("pin_user_id"))
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
                        .mediaId(rs.getLong("media_id"))
                        .userId(rs.getLong("pin_user_id"))
                        .build();
                board.getPins().add(pin);
            }
        }

        return new ArrayList<>(boardMap.values());
    }
}
