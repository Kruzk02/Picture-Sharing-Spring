package com.app.Service;

import com.app.DAO.CommentDao;
import com.app.DAO.PinDao;
import com.app.DTO.PinDTO;
import com.app.Model.Comment;
import com.app.Model.Pin;
import com.app.Model.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final PinDao pinDao;
    private final CommentDao commentDao;
    private final ModelMapper modelMapper;

    /**
     * Constructs a new PinService.
     *
     * @param pinDao The PinDaoImpl for accessing pin related data.
     * @param modelMapper The ModelMapper for entity-DTO mapping.
     */
    @Autowired
    public PinService(PinDao pinDao, CommentDao commentDao, ModelMapper modelMapper) {
        this.pinDao = pinDao;
        this.commentDao = commentDao;
        this.modelMapper = modelMapper;
    }

    /**
     * Retrieves all pins.
     *
     * @return A List of all pins.
     */

    public List<Pin> getAllPins(){
        return pinDao.getAllPins();
    }

    public List<Comment> getAllCommentByPinId(Long pinId){
        return commentDao.findByPinId(pinId);
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
    public Pin save(User user, PinDTO pinDTO, MultipartFile multipartFile) throws IOException {
        Path uploadPath = Paths.get("upload");
        if(!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        String fileCode = RandomStringUtils.randomAlphabetic(8)+".png";

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileCode);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            Pin pin = modelMapper.map(pinDTO,Pin.class);
            pin.setImage_url(filePath.toString());
            pin.setFileName(fileCode);
            pin.setUser(user);
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

    public Pin findById(Long id){
        return pinDao.findById(id);
    }

    /**
     * Deletes a Pin by its ID, if it exists.
     *
     * @param id The ID of the pin to delete.
     */
    public void deleteById(Long id) throws IOException {
        Pin pin = pinDao.findById(id);
        if(pin != null){
            Files.deleteIfExists(Path.of(pin.getImage_url()));
            pinDao.deleteById(id);
        }
    }
}
