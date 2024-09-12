package com.app.Service;

import com.app.DAO.Impl.CommentDaoImpl;
import com.app.DAO.Impl.PinDaoImpl;
import com.app.DTO.CommentDTO;
import com.app.Model.Comment;
import com.app.Model.Pin;
import com.app.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentDaoImpl commentDao;

    @Mock
    private PinDaoImpl pinDao;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CommentService commentService;

    @Test
    public void testSaveComment() {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setUser(new User(1L,"phuc","phuc@gmail.com","123",null));
        commentDTO.setPin(new Pin());
        commentDTO.setContent("Comment Content");

        Long pinId = commentDTO.getPin().getId();
        Pin pin = pinDao.findById(pinId);

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setUser(comment.getUser());
        comment.setPin(pin);

        when(modelMapper.map(commentDTO, Comment.class)).thenReturn(comment);
        when(commentDao.save(comment)).thenReturn(comment);

        Comment savedComment = commentService.save(commentDTO);

        assertEquals(comment.getContent(), savedComment.getContent());
        verify(modelMapper, times(1)).map(commentDTO, Comment.class);
        verify(commentDao, times(1)).save(comment);
    }

    @Test
    public void testDeleteById() {
        Long commentId = 1L;
        Comment comment = new Comment();
        comment.setId(commentId);

        when(commentDao.findById(commentId)).thenReturn(comment);

        commentService.deleteById(commentId);

        verify(commentDao, times(1)).findById(commentId);
        verify(commentDao, times(1)).deleteById(commentId);
    }

    @Test
    public void testDeleteByIdWhenCommentNotFound() {
        Long commentId = 1L;

        when(commentDao.findById(commentId)).thenReturn(null);

        commentService.deleteById(commentId);

        verify(commentDao, times(1)).findById(commentId);
        verify(commentDao, never()).deleteById(commentId);
    }
}
