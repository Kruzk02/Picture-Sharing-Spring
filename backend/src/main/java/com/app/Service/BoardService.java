package com.app.Service;

import com.app.DTO.request.BoardRequest;
import com.app.Model.Board;

import java.util.List;

public interface BoardService {
    Board save(BoardRequest boardRequest);
    Board addPinToBoard(Long pinId, Long boardId);
    Board deletePinFromBoard(Long pinId, Long boardId);
    Board update(Long id, String name);
    Board findById(Long id);
    List<Board> findAllByUserId(Long userId, int limit, int offset);
    void deleteIfUserMatches(Long id);
}
