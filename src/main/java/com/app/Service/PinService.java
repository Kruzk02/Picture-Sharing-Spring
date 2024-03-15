package com.app.Service;

import com.app.DAO.Impl.PinDaoImpl;
import com.app.DTO.PinDTO;
import com.app.Model.Pin;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Pin Service class responsible for handling operations relates to Pins.<p>
 * This class interacts with the PinDaoImpl for data access,
 * and utilizes ModelMapper for mapping between DTOs and entity objects.
 */
@Service
public class PinService {

    private final PinDaoImpl pinDao;
    private final ModelMapper modelMapper;

    /**
     * Constructs a new PinService.
     *
     * @param pinDao The PinDaoImpl for accessing pin related data.
     * @param modelMapper The ModelMapper for entity-DTO mapping.
     */
    @Autowired
    public PinService(PinDaoImpl pinDao, ModelMapper modelMapper) {
        this.pinDao = pinDao;
        this.modelMapper = modelMapper;
    }

    /**
     * Retrieves all pins.
     *
     * @return A List of all pins.
     */
    @Cacheable("pins")
    public List<Pin> getAllPins(){
        return pinDao.getAllPins();
    }

    /**
     * Saves a new pin with a provided PinDTO and MultipartFile. <p>
     * Saves the upload file to the "upload" directory and sets the image URL and file name to pin.
     *
     * @param pinDTO The PinDTO object containing pin information.
     * @param multipartFile The MultipartFile containing the upload image file.
     * @return The saved Pin entity.
     * @throws IOException If an I/O error occurs while saving the file.
     */
    public Pin save(PinDTO pinDTO, MultipartFile multipartFile) throws IOException {
        Path uploadPath = Paths.get("upload");
        if(!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        String fileCode = RandomStringUtils.randomAlphabetic(8)+".png";

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileCode);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            Pin pin = modelMapper.map(pinDTO,Pin.class);
            pin.setImage_url(filePath.toString());
            pin.setFileName(fileCode);
            return pinDao.save(pin);
        } catch (IOException e) {
            throw new IOException("Could not save file: " + fileCode, e);
        }
    }

    /**
     * Retrieves a pin by its ID.
     *
     * @param id The ID of the pin to retrieve.
     * @return The Pin entity corresponding to the provided ID.
     */
    @Cacheable("pin")
    public Pin findById(Long id){
        return pinDao.findById(id);
    }

    /**
     * Deletes a Pin by its ID, if it exists.
     *
     * @param id The ID of the pin to delete.
     */
    public void deleteById(Long id){
        Pin pin = pinDao.findById(id);
        if(pin != null){
            pinDao.deleteById(id);
        }
    }
}
