package com.app.Service.impl;

import com.app.DAO.BoardDao;
import com.app.DAO.PinDao;
import com.app.DAO.UserDao;
import com.app.DTO.request.BoardRequest;
import com.app.Model.Board;
import com.app.Model.Gender;
import com.app.Model.Pin;
import com.app.Model.User;
import com.app.exception.sub.BoardNotFoundException;
import com.app.exception.sub.NameValidationException;
import com.app.exception.sub.PinNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest {

    @Mock private BoardDao boardDao;
    @Mock private PinDao pinDao;
    @Mock private UserDao userDao;

    @InjectMocks private BoardServiceImpl boardService;

    private User user;
    private Pin pin;
    private Board board;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .username("username")
                .email("email@gmail.com")
                .password("encodedPassword")
                .gender(Gender.OTHER)
                .build();

        pin = Pin.builder()
                .id(1L)
                .description("description")
                .userId(1L)
                .mediaId(1L)
                .build();

        board = Board.builder()
                .id(1L)
                .user(user)
                .name("name")
                .pins(List.of(pin))
                .build();
    }

    @Test
    void save_shouldSaveBoardSuccessfully() {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("username");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(pinDao.findById(1L, false)).thenReturn(pin);

        Mockito.when(boardDao.save(Mockito.argThat(b -> b.getName() != null))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = boardService.save(new BoardRequest(new long[]{1}, "name"));

        assertNotNull(result);
        assertEquals(board.getName(), result.getName());
    }

    @Test
    void save_shouldThrowException_whenPinNotFound() {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("username");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(PinNotFoundException.class, () -> boardService.save(new BoardRequest(new long[]{1}, "name")));
    }

    @Test
    void findById_shouldReturnBoard() {
        Mockito.when(boardDao.findById(1L)).thenReturn(board);
        var result = boardService.findById(1L);

        assertNotNull(result);
        assertEquals(board, result);
    }

    @Test
    void findById_shouldThrowException_whenBoardNotFound() {
        Mockito.when(boardDao.findById(1L)).thenReturn(null);
        assertThrows(BoardNotFoundException.class, () -> boardService.findById(1L));
    }

    @Test
    void findAllByUserId_shouldReturnListOfBoard() {
        Mockito.when(boardDao.findAllByUserId(1L, 10, 0)).thenReturn(List.of(board));
        var result = boardService.findAllByUserId(1L, 10, 0);

        assertNotNull(result);
        assertEquals(List.of(board), result);
    }

    @Test
    void findAllByUserId_shouldReturnEmptyList() {
        Mockito.when(boardDao.findAllByUserId(1L, 10, 0)).thenReturn(Collections.emptyList());
        var result = boardService.findAllByUserId(1L, 10, 0);

        assertEquals(Collections.emptyList(), result);
    }
}