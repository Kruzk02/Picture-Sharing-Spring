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

    private Comment comment;
    private User user;
    private Pin pin;
    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("test")
                .email("test@gmail.com")
                .password("test")
                .build();
        pin = Pin.builder()
                .id(1L)
                .description("NOPE")
                .fileName("YES")
                .image_url("/upload")
                .userId(1L)
                .build();
        comment = Comment.builder()
                .id(1L)
                .content("HELLO WORLD")
                .user(user)
                .pin(pin)
                .build();
    }

    @Test
    public void testSaveComment() {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setUser(user);
        commentDTO.setPin(pin);
        commentDTO.setContent("HELLO WORLD");

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
        when(commentDao.findById(comment.getId())).thenReturn(comment);

        commentService.deleteIfUserMatches(user,comment.getId());
        verify(commentDao).deleteById(comment.getId());
    }

}
