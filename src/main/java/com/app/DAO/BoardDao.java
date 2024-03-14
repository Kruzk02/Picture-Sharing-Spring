package com.app.DAO;

import com.app.Model.Board;

/**
 * Interface for managing Board data access operations.
 */
public interface BoardDao {

    /**
     * Saves a board object into the database and associates it with the specified pin.
     *
     * @param board The board object to be saved.
     * @param pinId The id of the pin to associate the board with.
     * @return The saved board object.
     */
    Board save(Board board, Long pinId);

    /**
     * Finds a board by its id.
     *
     * @param id The id of the board to be found.
     * @return The board object if found, otherwise null.
     */
    Board findById(Long id);

    /**
     * Deletes a board from the database by its id.
     *
     * @param id The id of the board to be deleted.
     * @return The number of boards deleted (should be 1 if successful, 0 if not found).
     */
    int deleteById(Long id);
}
