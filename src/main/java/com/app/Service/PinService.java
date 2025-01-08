package com.app.Service;

import com.app.DTO.request.PinRequest;
import com.app.Model.Pin;

import java.io.IOException;
import java.util.List;

public interface PinService {

    List<Pin> getAllPins(int offset);
    Pin save(PinRequest pinRequest);
    Pin update(Long id, PinRequest pinRequest);
    Pin findBasicById(Long id);
    Pin findFullById(Long id);
    List<Pin> findNewestPin(int limit, int offset);
    List<Pin> findOldestPin(int limit, int offset);
    List<Pin> findPinByUserId(Long userId, int limit, int offset);
    void delete(Long id) throws IOException;
}
