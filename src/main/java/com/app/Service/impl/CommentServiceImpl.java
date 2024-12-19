package com.app.Service;

import com.app.DAO.Impl.CommentDaoImpl;
import com.app.DAO.UserDao;
import com.app.DTO.request.CreateCommentRequest;
import com.app.Model.Comment;
import com.app.Model.User;
import com.app.exception.sub.UserNotMatchException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class CommentServiceImpl {

    private final CommentDaoImpl commentDao;
    private final UserDao userDao;

    public Comment save(CreateCommentRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Comment comment = Comment.builder()
                .content(request.content())
                .pinId(request.pinId())
                .userId(userDao.findUserByUsername(authentication.getName()).getId())
                .build();
        return commentDao.save(comment);
    }

    public void delete(Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDao.findUserByUsername(authentication.getName());

        Comment comment = commentDao.findBasicById(id);
        if(comment != null && Objects.equals(user.getId(),comment.getUserId())){
            commentDao.deleteById(comment.getId());
        } else {
            throw new UserNotMatchException("User does not match with a comment");
        }
    }
}
