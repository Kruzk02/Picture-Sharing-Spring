package com.app.Service.impl;

import com.app.DAO.BoardDao;
import com.app.DAO.PinDao;
import com.app.DAO.UserDao;
import com.app.DTO.request.BoardRequest;
import com.app.Model.Board;
import com.app.Model.Pin;
import com.app.Model.User;
import com.app.Service.BoardService;
import com.app.exception.sub.*;
import java.util.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Board Service class responsible for handling operations related to boards.
 *
 * <p>This class interacts with the BoardDaoImpl and PinDaoImpl for data access, and utilizes
 * ModelMapper for mapping between DTOs and entity objects.
 */
@Service
@Qualifier("boardServiceImpl")
@AllArgsConstructor
public class BoardServiceImpl implements BoardService {

  private final BoardDao boardDao;
  private final PinDao pinDao;
  private final UserDao userDao;

  private User getAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return userDao.findUserByUsername(authentication.getName());
  }

  /**
   * Saves a new board based on the provided boardDTO.
   *
   * <p>Retrieves the corresponding pin from the pinDao, maps the boardDTO to a Board entity.
   *
   * <p>Sets the retrieved pin to the board, and saves the board using boardDao.
   *
   * @param boardRequest the boardRequest object containing board information.
   * @return The saved Board entity.
   */
  @Override
  public Board save(BoardRequest boardRequest) {
    if (boardRequest.name() == null) {
      throw new NameValidationException("Board name shouldn't be empty");
    }

    if (boardRequest.name().length() <= 3 || boardRequest.name().length() >= 256) {
      throw new NameValidationException(
          "Board name should be longer than 3 characters and less than 256 characters");
    }

    Board board = Board.builder().name(boardRequest.name()).user(getAuthenticatedUser()).build();

    if (boardRequest.pin_id() != null && boardRequest.pin_id().length > 0) {
      List<Pin> pins = new ArrayList<>();

      for (Long pinId : boardRequest.pin_id()) {
        Pin pin = pinDao.findById(pinId, false);
        if (pin == null) {
          throw new PinNotFoundException("Pin not found with a id: " + pinId);
        }

        pins.add(pin);
      }

      board.setPins(pins);
    } else {
      board.setPins(Collections.emptyList());
    }

    return boardDao.save(board);
  }

  @Override
  public Board addPinToBoard(Long pinId, Long boardId) {
    Pin pin = pinDao.findById(pinId, false);
    if (pin == null) {
      throw new PinNotFoundException("Pin not found with ID: " + pinId);
    }

    Board board = boardDao.findById(boardId);
    if (board == null) {
      throw new BoardNotFoundException("Board not found with ID: " + boardId);
    }

    if (!board.getUser().getId().equals(getAuthenticatedUser().getId())) {
      throw new UserNotMatchException("Authenticated user does not own this board");
    }

    if (board.getPins().contains(pin)) {
      throw new PinAlreadyExistingException("Pin already exists in the board");
    }

    return boardDao.addPinToBoard(pin, board);
  }

  @Override
  public Board deletePinFromBoard(Long pinId, Long boardId) {
    Pin pin = pinDao.findById(pinId, false);
    if (pin == null) {
      throw new PinNotFoundException("Pin not found with ID: " + pinId);
    }

    Board board = boardDao.findById(boardId);
    if (board == null) {
      throw new BoardNotFoundException("Board not found with ID: " + boardId);
    }

    if (!board.getUser().getId().equals(getAuthenticatedUser().getId())) {
      throw new UserNotMatchException("Authenticated user does not own this board");
    }

    if (board.getPins().stream().noneMatch(pin::equals)) {
      throw new PinNotInBoardException("Pin not found in a board");
    }

    return boardDao.deletePinFromBoard(pin, board);
  }

  @Override
  public Board update(Long id, String name) {
    Board existingBoard = boardDao.findById(id);
    if (existingBoard == null) {
      throw new BoardNotFoundException("Board not found with a id: " + id);
    }

    if (!Objects.equals(existingBoard.getUser().getId(), getAuthenticatedUser().getId())) {
      throw new UserNotMatchException("Authenticated user not own this board");
    }

    existingBoard.setName(name != null ? name : existingBoard.getName());

    return boardDao.update(existingBoard, id);
  }

  /**
   * Retrieves a board by its ID.
   *
   * @param id The ID of the board to retrieve.
   * @return The Board entity corresponding to the provided ID.
   */
  @Override
  public Board findById(Long id) {
    Board board = boardDao.findById(id);
    if (board == null) {
      throw new BoardNotFoundException("Board not found with a id: " + id);
    }
    return board;
  }

  @Override
  public List<Board> findAllByUserId(Long userId, int limit, int offset) {
    List<Board> boards = boardDao.findAllByUserId(userId, limit, offset);
    if (boards.isEmpty()) {
      return Collections.emptyList();
    }

    return boards;
  }

  /**
   * Deletes a board by its ID if user id match with board.
   *
   * @param id The ID of the board to delete.
   */
  @Override
  public void deleteIfUserMatches(Long id) {
    Board board =
        Optional.ofNullable(boardDao.findById(id))
            .orElseThrow(() -> new BoardNotFoundException("Board not found with a id"));

    if (!Objects.equals(board.getUser().getId(), getAuthenticatedUser().getId())) {
      throw new UserNotMatchException("Authenticated user does not own this board");
    }

    boardDao.deleteById(id);
  }
}
