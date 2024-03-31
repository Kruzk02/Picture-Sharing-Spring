package com.app.Service;

import com.app.DAO.Impl.CommentDaoImpl;
import com.app.DAO.Impl.PinDaoImpl;
import com.app.DTO.CommentDTO;
import com.app.Model.Comment;
import com.app.Model.Pin;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentDaoImpl commentDao;
    private final PinDaoImpl pinDao;
    private final ModelMapper modelMapper;

    @Autowired
    public CommentService(CommentDaoImpl commentDao, PinDaoImpl pinDao, ModelMapper modelMapper) {
        this.commentDao = commentDao;
        this.pinDao = pinDao;
        this.modelMapper = modelMapper;
    }

    public Comment save(CommentDTO commentDTO){
        Long pinId = commentDTO.getPinId();
        Pin pin = pinDao.findById(pinId);

        Comment comment = modelMapper.map(commentDTO,Comment.class);
        comment.setPin(pin);
        return commentDao.save(comment);
    }

    public Comment findByID(Long id){
        return commentDao.findById(id);
    }

    public void deleteById(Long id){
        Comment comment = commentDao.findById(id);
        if(comment != null){
            commentDao.deleteById(comment.getId());
        }
    }
}
