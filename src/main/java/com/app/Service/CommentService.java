package com.app.Service;

import com.app.DAO.Impl.CommentDaoImpl;
import com.app.DTO.CommentDTO;
import com.app.Model.Comment;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentDaoImpl commentDao;
    private final ModelMapper modelMapper;

    @Autowired
    public CommentService(CommentDaoImpl commentDao, ModelMapper modelMapper) {
        this.commentDao = commentDao;
        this.modelMapper = modelMapper;
    }

    public Comment save(CommentDTO commentDTO){
        Comment comment = modelMapper.map(commentDTO,Comment.class);
        return commentDao.save(comment);
    }

    public void deleteById(Long id){
        Comment comment = commentDao.findById(id);
        if(comment != null){
            commentDao.deleteById(comment.getId());
        }
    }
}
