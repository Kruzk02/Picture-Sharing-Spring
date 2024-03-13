package com.app.DAO;

import com.app.Model.Board;

public interface BoardDao {
    Board save(Board board,Long pinId);
    Board findById(Long id);
    int deleteById(Long id);
}
