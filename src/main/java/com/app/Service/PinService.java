package com.app.Service;

import com.app.DAO.Impl.PinDaoImpl;
import com.app.DTO.PinDTO;
import com.app.Model.Pin;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PinService {

    private final PinDaoImpl pinDao;
    private final ModelMapper modelMapper;

    @Autowired
    public PinService(PinDaoImpl pinDao, ModelMapper modelMapper) {
        this.pinDao = pinDao;
        this.modelMapper = modelMapper;
    }

    public List<Pin> getAllPins(){
        return pinDao.getAllPins();
    }

    public Pin save(PinDTO pinDTO){
        Pin pin = modelMapper.map(pinDTO,Pin.class);
        return pinDao.save(pin);
    }

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
