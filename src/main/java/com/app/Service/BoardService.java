package com.app.Service;

import com.app.DAO.Impl.BoardDaoImpl;
import com.app.DAO.Impl.PinDaoImpl;
import com.app.DTO.BoardDTO;
import com.app.Model.Board;
import com.app.Model.Pin;
import com.app.Model.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class BoardService {

    private final BoardDaoImpl boardDao;
    private final PinDaoImpl pinDao;
    private final ModelMapper modelMapper;

    @Autowired
    public BoardService(BoardDaoImpl boardDao, PinDaoImpl pinDao, ModelMapper modelMapper) {
        this.boardDao = boardDao;
        this.pinDao = pinDao;
        this.modelMapper = modelMapper;
    }

    public Board save(BoardDTO boardDTO){
        Pin pin = pinDao.findById(boardDTO.getPin_id());
        Board board = modelMapper.map(boardDTO,Board.class);
        board.setPins(Arrays.asList(pin));
        return boardDao.save(board);
    }

    @Cacheable("board")
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
