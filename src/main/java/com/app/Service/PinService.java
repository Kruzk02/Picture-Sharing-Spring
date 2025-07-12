package com.app.Service;

import com.app.DTO.request.PinRequest;
import com.app.Model.Pin;
import com.app.Model.SortType;
import java.io.IOException;
import java.util.List;

public interface PinService {

  List<Pin> getAllPins(SortType sortType, int limit, int offset);

  List<Pin> getAllPinsByHashtag(String tag, int limit, int offset);

  Pin save(PinRequest pinRequest);

  Pin update(Long id, PinRequest pinRequest);

  Pin findById(Long id, boolean fetchDetails);

  List<Pin> findPinByUserId(Long userId, int limit, int offset);

  void delete(Long id) throws IOException;
}
