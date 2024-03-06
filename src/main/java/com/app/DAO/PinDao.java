package com.app.DAO;

import com.app.Model.Pin;

import java.util.List;

public interface PinDao {
    List<Pin> getAllPins();
    Pin save(Pin pin);
    Pin findById(Long id);
    int deleteById(Long id);
}
