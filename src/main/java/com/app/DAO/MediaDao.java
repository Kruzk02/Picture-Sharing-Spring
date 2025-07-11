package com.app.DAO;

import com.app.Model.Media;

/** Data Access Object interface for managing Media entities. */
public interface MediaDao {
  /**
   * Save a new Media
   *
   * @param media The media to be saved.
   * @return the saved media
   */
  Media save(Media media);

  /**
   * Update a existing media.
   *
   * @param id The ID of media to be updated
   * @param media The media to be saved
   * @return the updated media
   */
  Media update(Long id, Media media);

  /**
   * Find a media by its ID.
   *
   * @param id The ID of the media
   * @return The media if found, or exception if not found.
   */
  Media findById(Long id);

  /**
   * Find a media by comment ID.
   *
   * @param commentId The ID of the comment
   * @return the media if found, or exception if not found
   */
  Media findByCommentId(Long commentId);

  /**
   * Delete a media by its ID.
   *
   * @param id the ID of media
   * @return The number of row affected.
   */
  int deleteById(Long id);
}
