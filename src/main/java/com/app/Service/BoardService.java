package com.app.Service;

import com.app.DTO.request.BoardRequest;
import com.app.Model.Board;

import java.util.List;

public interface BoardService {
    Board save(BoardRequest boardRequest);
    Board update(Long id, BoardRequest boardRequest);
    Board findById(Long id);
    List<Board> findAllByUserId(Long userId, int limit, int offset);
    void deleteIfUserMatches(Long id);
}
