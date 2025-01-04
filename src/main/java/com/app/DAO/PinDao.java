package com.app.DAO;

import com.app.Model.Pin;
import java.util.List;

/**
 * Interface for managing Pin data access operations.
 */
public interface PinDao {

    /**
     * Retrieves all pins stored in the database.
     *
     * @return A list of all pins stored in the database.
     */
    List<Pin> getAllPins(int offset);

    /**
     * Saves a pin object into the database.
     *
     * @param pin The pin object to be saved.
     * @return The saved pin object.
     */
    Pin save(Pin pin);

    Pin update(Long id, Pin pin);
    /**
     * Finds a basic detail pin by its id.
     *
     * @param id The id of the pin to be found.
     * @return The pin object if found, otherwise null.
     */
    Pin findBasicById(Long id);

    /**
     * Find a full detail pin by its id.
     *
     * @param id The id of the pin to be found.
     * @return The pin object if found, otherwise throw exception.
     */
    Pin findFullById(Long id);

    /**
     * Find the newest pin.
     * @param limit The maximum number of results to return
     * @return a list of newest pin.
     */
    List<Pin> findNewestPin(int limit, int offset);

    /**
     * Find the oldest pin.
     * @param limit The maximum number of result to return
     * @return a list of oldest pin.
     */
    List<Pin> findOldestPin(int limit, int offset);

    /**
     * Find the pin that associated with a user id.
     * @param userId The id of user.
     * @param limit The maximum number of result to return
     * @return a list of pin.
     */
    List<Pin> findPinByUserId(Long userId, int limit, int offset);

    /**
     * Find a user id by pin id
     * @param pinId the id of the pin
     * @return The user object if found, otherwise throw PinNotFoundException
     */
    Pin findUserIdByPinId(Long pinId);

    /**
     * Deletes a pin from the database by its id.
     *
     * @param id The id of the pin to be deleted.
     * @return The number of pins deleted (should be 1 if successful, 0 if not found).
     */
    int deleteById(Long id);
}
