package com.app.Service;

import com.app.DAO.Impl.CommentDaoImpl;
import com.app.DTO.CommentDTO;
import com.app.Model.Comment;
import com.app.Model.User;
import com.app.exception.sub.UserNotMatchException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

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

    public void deleteIfUserMatches(User user, Long id){
        Comment comment = commentDao.findById(id);
        if(comment != null && Objects.equals(user.getId(),comment.getUser().getId())){
            commentDao.deleteById(comment.getId());
        } else {
            throw new UserNotMatchException("User does not match with a comment");
        }
    }
}
