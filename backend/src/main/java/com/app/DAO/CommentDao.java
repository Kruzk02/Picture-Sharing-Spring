package com.app.DAO;

import com.app.Model.Comment;
import com.app.Model.SortType;

import java.util.List;

/**
 * Data Access Object interface for managing Comment entities.
 */
public interface CommentDao {

    /**
     * Saves a new comment.
     * @param comment The comment to be saved
     * @return the saved comment
     */
    Comment save(Comment comment);

    /**
     * Update a existing comment
     * @param id The ID of the comment to be updated
     * @param comment The comment to be saved
     * @return The updated comment
     */
    Comment update(Long id,Comment comment);

    /**
     * Find a comment by its ID.
     * @param id The ID of the comment
     * @param fetchDetails The basic or full details of the comment
     * @return The comment if found, or exception if not found
     */
    Comment findById(Long id, boolean fetchDetails);

    /**
     * Retrieves a list of comments by the ID of the associated pin.
     * @param pinId The id of the pin
     * @param sortType The sort newest or oldest
     * @param limit The maximum number of result to return
     * @param offset the starting point for pagination
     * @return a list of comments
     */
    List<Comment> findByPinId(Long pinId, SortType sortType, int limit, int offset);

    /**
     * Deletes a comment by its id.
     * @param id The id of the comment to delete
     * @return the number of rows affected
     */
    int deleteById(Long id);

}
