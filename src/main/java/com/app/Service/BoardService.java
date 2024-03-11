package com.app.Service;

import com.app.DAO.Impl.BoardDaoImpl;
import com.app.DTO.BoardDTO;
import com.app.Model.Board;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoardService {

    private final BoardDaoImpl boardDao;
    private final ModelMapper modelMapper;

    @Autowired
    public BoardService(BoardDaoImpl boardDao, ModelMapper modelMapper) {
        this.boardDao = boardDao;
        this.modelMapper = modelMapper;
    }

    public Board save(BoardDTO boardDTO){
        Board board = modelMapper.map(boardDTO,Board.class);
        return boardDao.save(board);
    }

    public Board findById(Long id){
        return boardDao.findById(id);
    }

    public void deleteById(Long id){
        Board board = boardDao.findById(id);
        if(board != null){
            boardDao.deleteById(id);
        }
    }
}
