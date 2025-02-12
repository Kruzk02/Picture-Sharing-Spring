package com.app.Service.impl;

import com.app.DAO.BoardDao;
import com.app.DAO.PinDao;
import com.app.DAO.UserDao;
import com.app.DTO.request.BoardRequest;
import com.app.Model.Board;
import com.app.Model.Pin;
import com.app.Model.User;
import com.app.Service.BoardService;
import com.app.exception.sub.BoardNotFoundException;
import com.app.exception.sub.PinIsEmptyException;
import com.app.exception.sub.UserNotMatchException;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

/**
 * Board Service class responsible for handling operations related to boards.<p>
 * This class interacts with the BoardDaoImpl and PinDaoImpl for data access,
 * and utilizes ModelMapper for mapping between DTOs and entity objects.
 */
@Service
@AllArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardDao boardDao;
    private final PinDao pinDao;
    private final UserDao userDao;
    private final RedisTemplate<String,Board> boardRedisTemplate;

    /**
     * Saves a new board based on the provided boardDTO.
     * <p>
     * Retrieves the corresponding pin from the pinDao, maps the boardDTO to a Board entity.
     * <p>
     * Sets the retrieved pin to the board, and saves the board using boardDao.
     *
     * @param boardRequest the boardRequest object containing board information.
     * @return The saved Board entity.
     */
    @Override
    public Board save(BoardRequest boardRequest) {
        if (boardRequest.pin_id() == null) {
            throw new PinIsEmptyException("Pin should not empty");
        }

        List<Pin> pins = new ArrayList<>();

        for (Long pinId : boardRequest.pin_id()) {
            Pin pin = pinDao.findBasicById(pinId);
            if (pin != null) {
                pins.add(pin);
            }
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDao.findUserByUsername(authentication.getName());

        Board board = Board.builder()
                .name(boardRequest.name())
                .user(user)
                .build();

        board.setPins(pins);
        return boardDao.save(board);
    }

    @Override
    public Board update(Long id, BoardRequest boardRequest) {
        Board existingBoard = boardDao.findById(id);
        if (existingBoard == null) {
            throw new BoardNotFoundException("Board not found with a id: " + id);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDao.findUserByUsername(authentication.getName());

        if (!Objects.equals(existingBoard.getUser().getId(), user.getId())) {
            throw new UserNotMatchException("Authenticated user not own this board");
        }

        List<Pin> pins = new ArrayList<>();

        for (Long pinId : boardRequest.pin_id()) {
            Pin pin = pinDao.findBasicById(pinId);
            if (pin != null) {
                pins.add(pin);
            }
        }

        existingBoard.setName(boardRequest.name() != null ? boardRequest.name(): existingBoard.getName());
        existingBoard.setPins(pins);

        return boardDao.update(existingBoard, id);
    }

    /**
     * Retrieves a board by its ID.
     *
     * @param id The ID of the board to retrieve.
     * @return The Board entity corresponding to the provided ID.
     */
    @Override
    public Board findById(Long id){
        Board cacheBoard = boardRedisTemplate.opsForValue().get("board:" + id);
        if (cacheBoard != null) return cacheBoard;

        Board board = boardDao.findById(id);
        if (board != null) {
            System.out.println(board.getId());
            boardRedisTemplate.opsForValue().set("board:" + board.getId(),board, Duration.ofHours(2));
        }

        return board;
    }

    @Override
    public List<Board> findAllByUserId(Long userId, int limit, int offset) {
        String cacheKeys = "board:user:" + userId;
        List<Board> cachedBoards = boardRedisTemplate.opsForList().range(cacheKeys, offset, offset + limit - 1);
        if (cachedBoards != null && !cachedBoards.isEmpty()) {
            return cachedBoards;
        }

        List<Board> boards = boardDao.findAllByUserId(userId,limit,offset);
        if (boards.isEmpty()) {
            return Collections.emptyList();
        }

        boardRedisTemplate.opsForList().rightPushAll(cacheKeys, boards);
        boardRedisTemplate.expire(cacheKeys, Duration.ofHours(2));

        return boards;
    }

    /**
     * Deletes a board by its ID if user id match with board.
     *
     * @param id The ID of the board to delete.
     */
    @Override
    public void deleteIfUserMatches(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDao.findUserByUsername(authentication.getName());
        Board board = Optional.ofNullable(boardDao.findById(id))
                .orElseThrow(() -> new BoardNotFoundException("Board not found with a id"));

        if(!Objects.equals(board.getUser().getId(), user.getId())){
            throw new UserNotMatchException("Authenticated user does not own this board");
        }

        boardDao.deleteById(id);
    }
}
