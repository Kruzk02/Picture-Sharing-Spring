package com.app.Service;

import com.app.DAO.Impl.BoardDaoImpl;
import com.app.DAO.Impl.PinDaoImpl;
import com.app.DTO.BoardDTO;
import com.app.Model.Board;
import com.app.Model.Pin;
import com.app.Model.User;
import com.app.exception.sub.UserNotMatchException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Board Service class responsible for handling operations related to boards.<p>
 * This class interacts with the BoardDaoImpl and PinDaoImpl for data access,
 * and utilizes ModelMapper for mapping between DTOs and entity objects.
 */
@Service
public class BoardService {

    private final BoardDaoImpl boardDao;
    private final PinDaoImpl pinDao;
    private final ModelMapper modelMapper;
    private final RedisTemplate<Object,Object> redisTemplate;

    /**
     * Constructs a new BoardService.
     *
     * @param boardDao The BoardDaoImpl for accessing board related data.
     * @param pinDao The PinDaoImpl for accessing pin related data.
     * @param modelMapper The ModelMapper for entity-DTO mapping.
     */
    @Autowired
    public BoardService(BoardDaoImpl boardDao, PinDaoImpl pinDao, ModelMapper modelMapper, RedisTemplate<Object, Object> redisTemplate) {
        this.boardDao = boardDao;
        this.pinDao = pinDao;
        this.modelMapper = modelMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Saves a new board based on the provided boardDTO.
     * <p>
     * Retrieves the corresponding pin from the pinDao, maps the boardDTO to a Board entity.
     * <p>
     * Sets the retrieved pin to the board, and saves the board using boardDao.
     *
     * @param boardDTO the BoardDTO object containing board information.
     * @return The saved Board entity.
     */
    public Board save(BoardDTO boardDTO){
        Pin pin = pinDao.findById(boardDTO.getPin_id());
        Board board = modelMapper.map(boardDTO,Board.class);
        board.setPins(Arrays.asList(pin));
        return boardDao.save(board,pin.getId());
    }

    /**
     * Retrieves a board by its ID.
     *
     * @param id The ID of the board to retrieve.
     * @return The Board entity corresponding to the provided ID.
     */

    public Board findById(Long id){
        Board cacheBoard = (Board) redisTemplate.opsForValue().get("board:" + id);
        if (cacheBoard != null) return cacheBoard;

        Board board = boardDao.findById(id);
        if (board != null) {
            redisTemplate.opsForValue().set("board:" + board.getId(),board, Duration.ofHours(2));
        }
        return board;
    }

    public List<Board> findAllByUserId(Long userId) {
        List<Board> boards = boardDao.findAllByUserId(userId);
        List<Object> cacheKeys = boards.stream()
                .map(board -> "board:" + board.getId())
                .collect(Collectors.toList());
        List<Object> cacheBoards = redisTemplate.opsForValue().multiGet(cacheKeys);

        for (int i = 0; i < Objects.requireNonNull(cacheBoards).size(); i++) {
            if (cacheBoards.get(i) == null) {
                redisTemplate.opsForValue().set("board:" + boards.get(i).getId(),boards.get(i),Duration.ofHours(2));
            }
        }
        return boards;
    }

    /**
     * Deletes a board by its ID if user id match with board.
     *
     * @param id The ID of the board to delete.
     */
    public void deleteIfUserMatches(User user,Long id){
        Board board = boardDao.findById(id);
        if(board != null && Objects.equals(board.getUser().getId(), user.getId())){
            boardDao.deleteById(id);
        } else {
            throw new UserNotMatchException("User does not matching with board owner");
        }
    }
}
