package com.app.DAO;

import com.app.Model.Board;
import com.app.Model.Pin;

public interface BoardDao {
    Board save(Board board);
    Board findById(Long id);
    int deleteById(Long id);
}
