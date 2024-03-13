package com.app.Service;

import com.app.DAO.Impl.PinDaoImpl;
import com.app.DAO.Impl.UserDaoImpl;
import com.app.DTO.PinDTO;
import com.app.Model.Pin;
import com.app.Model.Role;
import com.app.Model.User;
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
import java.util.Arrays;
import java.util.List;

@Service
public class PinService {

    private final PinDaoImpl pinDao;
    private final UserDaoImpl userDao;
    private final ModelMapper modelMapper;

    @Autowired
    public PinService(PinDaoImpl pinDao, UserDaoImpl userDao, ModelMapper modelMapper) {
        this.pinDao = pinDao;
        this.userDao = userDao;
        this.modelMapper = modelMapper;
    }

    @Cacheable("pins")
    public List<Pin> getAllPins(){
        return pinDao.getAllPins();
    }

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

    @Cacheable("pin")
    public Pin findById(Long id){
        return pinDao.findById(id);
    }

    public void deleteById(Long id){
        Pin pin = pinDao.findById(id);
        if(pin != null){
            pinDao.deleteById(id);
        }
    }
}
