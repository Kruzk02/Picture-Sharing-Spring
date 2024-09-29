package com.app.Service;

import com.app.DAO.SubCommentDao;
import com.app.DTO.SubCommentDTO;
import com.app.Model.Comment;
import com.app.Model.Pin;
import com.app.Model.SubComment;
import com.app.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubCommentServiceTest {
    @Mock private SubCommentDao subCommentDao;
    @Mock private ModelMapper modelMapper;
    @Mock private RedisTemplate<Object, Object> redisTemplate;
    @Mock private ValueOperations<Object, Object> valueOperations;
    @InjectMocks private SubCommentService subCommentService;

    private SubComment subComment;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("test")
                .email("test@gmail.com")
                .password("test")
                .build();

        subComment = SubComment.builder()
                .id(1L)
                .content("OK")
                .comment(Comment.builder()
                        .id(1L)
                        .content("HELLO WORLD")
                        .user(user)
                        .pin(Pin.builder()
                                .id(1L)
                                .description("NOPE")
                                .fileName("YES")
                                .image_url("/upload")
                                .userId(1L)
                                .build())
                        .build())
                .user(User.builder()
                        .id(1L)
                        .username("test")
                        .email("test@gmail.com")
                        .password("test")
                        .build())
                .timestamp(Timestamp.from(Instant.now()))
                .build();
    }

    @Test
    void testSaveSubComment() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        SubCommentDTO subCommentDTO = new SubCommentDTO();
        subCommentDTO.setComment(Comment.builder()
                .id(1L)
                .content("HELLO WORLD")
                .user(User.builder()
                        .id(1L)
                        .username("test")
                        .email("test@gmail.com")
                        .password("test")
                        .build())
                .pin(Pin.builder()
                        .id(1L)
                        .description("NOPE")
                        .fileName("YES")
                        .image_url("/upload")
                        .userId(1L)
                        .build()).build());
        subCommentDTO.setContent("WELL");

        when(modelMapper.map(subCommentDTO, SubComment.class)).thenReturn(subComment);
        when(subCommentDao.save(subComment)).thenReturn(subComment);

        SubComment result = subCommentService.save(subCommentDTO);
        assertEquals(subComment.getId(),result.getId());
        verify(valueOperations).set("subComment:1",subComment, Duration.ofHours(2));
        verify(subCommentDao).save(subComment);
    }

    @Test
    void testFindAllByCommentId() {
        Long commentId = 1L;

        when(subCommentDao.findAllByCommentId(commentId)).thenReturn(List.of(subComment));

        List<SubComment> result = subCommentDao.findAllByCommentId(commentId);

        assertEquals(1,result.size());
        verify(subCommentDao).findAllByCommentId(commentId);
    }

    @Test
    void testFindById() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("subComment:"+subComment.getId())).thenReturn(subComment);
        SubComment result = subCommentService.findById(subComment.getId());

        assertEquals(subComment.getId(),result.getId());
        verify(valueOperations).get("subComment:"+subComment.getId());
        verifyNoInteractions(subCommentDao);
    }

    @Test
    void deleteById() {
        when(subCommentDao.findById(subComment.getId())).thenReturn(subComment);
        subCommentService.deleteIfUserMatches(user,subComment.getId());
        verify(subCommentDao).deleteById(subComment.getId());
    }
}