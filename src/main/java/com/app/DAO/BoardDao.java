package com.app.DAO;

import com.app.Model.Board;
import com.app.Model.Pin;
import java.util.List;

/** Interface for managing Board data access operations. */
public interface BoardDao {

  /**
   * Saves a board object into the database and associates it with the specified pin or empty.
   *
   * @param board The board object to be saved.
   * @return The saved board object.
   */
  Board save(Board board);

  Board addPinToBoard(Pin pin, Board board);

  Board deletePinFromBoard(Pin pin, Board board);

  /**
   * Update an existing board.
   *
   * @param board The board object to be saved.
   * @param id id of the board.
   * @return The updated board object.
   */
  Board update(Board board, long id);

  /**
   * Finds a board by its id.
   *
   * @param id The id of the board to be found.
   * @return The board object if found, otherwise null.
   */
  Board findById(Long id);

  /**
   * Find all board by user id
   *
   * @param userId the user id of the board to be found
   * @return a list of board
   */
  List<Board> findAllByUserId(Long userId, int limit, int offset);

  /**
   * Deletes a board from the database by its id.
   *
   * @param id The id of the board to be deleted.
   * @return The number of boards deleted (should be 1 if successful, 0 if not found).
   */
  int deleteById(Long id);
}
