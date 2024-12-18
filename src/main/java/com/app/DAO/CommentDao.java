package com.app.DAO;

import com.app.Model.Comment;

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
     * Find a comment with basic details by its ID.
     * @param id The ID of the comment
     * @return The comment if found, or exception if not found
     */
    Comment findBasicById(Long id);

    /**
     * Find a comment with full details by its ID.
     * @param id The ID of the comment
     * @return The comment if found, or exception if not found
     */
    Comment findDetailsById(Long id);

    /**
     * Retrieves a list of comments by the ID of the associated pin.
     * @param pinId The id of the pin
     * @param limit The maximum number of result to return
     * @param offset the starting point for pagination
     * @return a list of comments
     */
    List<Comment> findByPinId(Long pinId, int limit, int offset);

    /**
     * Retrieves the newest comments for specific pin ID.
     * @param pinId The id of the pin
     * @param limit The maximum number of results to return
     * @return a list of newest comments
     */
    List<Comment> findNewestByPinId(Long pinId, int limit);

    /**
     * Retrieves the oldest comments for specific pin ID.
     * @param pinId The id of the pin
     * @param limit The maximum number of result to return
     * @return a list of oldest comments
     */
    List<Comment> findOldestByPinId(Long pinId, int limit);

    /**
     * Deletes a comment by its id.
     * @param id The id of the comment to delete
     * @return the number of rows affected
     */
    int deleteById(Long id);

    /**
     * Deeltes all comment associated with a specific pin id.
     * @param pinId The id of pin
     * @return The number of rows affected
     */
    int deleteByPinId(long pinId);

}
