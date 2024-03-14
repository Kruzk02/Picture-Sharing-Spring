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
    List<Pin> getAllPins();

    /**
     * Saves a pin object into the database.
     *
     * @param pin The pin object to be saved.
     * @return The saved pin object.
     */
    Pin save(Pin pin);

    /**
     * Finds a pin by its id.
     *
     * @param id The id of the pin to be found.
     * @return The pin object if found, otherwise null.
     */
    Pin findById(Long id);

    /**
     * Deletes a pin from the database by its id.
     *
     * @param id The id of the pin to be deleted.
     * @return The number of pins deleted (should be 1 if successful, 0 if not found).
     */
    int deleteById(Long id);
}
