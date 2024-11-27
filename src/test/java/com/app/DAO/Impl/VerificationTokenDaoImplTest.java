package com.app.DAO.Impl;

import com.app.Model.VerificationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationTokenDaoImplTest {

    @Mock
    private JdbcTemplate template;
    @InjectMocks
    private VerificationTokenDaoImpl verificationTokenDao;

    private VerificationToken verificationToken;
    private final String token = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        verificationToken = VerificationToken.builder()
                .id(1L)
                .token(token)
                .userId(1L)
                .expireDate(Date.valueOf(LocalDate.now()))
                .build();
    }

    @Test
    void testFindByToken_Success() {
        String sql = "SELECT token, user_id, expiration_date FROM verification_token WHERE token = ?";

        when(template.queryForObject(eq(sql), any(RowMapper.class), eq(token)))
                .thenReturn(verificationToken);

        VerificationToken result = verificationTokenDao.findByToken(token);

        assertNotNull(result);
        assertEquals(verificationToken.getToken(), result.getToken());
        verify(template).queryForObject(eq(sql), any(RowMapper.class), eq(token));
    }

    @Test
    void testFindByToken_NotFound() {
        String sql = "SELECT token, user_id, expiration_date FROM verification_token WHERE token = ?";

        when(template.queryForObject(eq(sql), any(RowMapper.class), eq(token)))
                .thenThrow(new EmptyResultDataAccessException(1));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> verificationTokenDao.findByToken(token));
        assertTrue(exception.getMessage().contains("Verification token not found"));
        verify(template).queryForObject(eq(sql), any(RowMapper.class), eq(token));
    }

    @Test
    void testCreate_Success() {
        long generatedId = 1L;

        when(template.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder passedKeyHolder = invocation.getArgument(1);
                    passedKeyHolder.getKeyList().add(Map.of("GENERATED_KEY", generatedId));
                    return 1;
                });

        VerificationToken result = verificationTokenDao.create(verificationToken);

        assertNotNull(result);
        assertEquals(verificationToken.getToken(), result.getToken());
        assertEquals(generatedId, result.getId());
        verify(template).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
    }

    @Test
    void testCreate_Failure() {
        when(template.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenReturn(0);

        assertThrows(DataAccessException.class,
                () -> verificationTokenDao.create(verificationToken));
        verify(template).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
    }

    @Test
    void testDeleteByToken_Success() {
        String sql = "DELETE FROM verification_token WHERE token = ?";

        when(template.update(eq(sql), eq(token))).thenReturn(1);

        int rowsDeleted = verificationTokenDao.deleteByToken(token);

        assertEquals(1, rowsDeleted);
        verify(template).update(eq(sql), eq(token));
    }

    @Test
    void testDeleteByToken_NotFound() {
        String sql = "DELETE FROM verification_token WHERE token = ?";

        when(template.update(eq(sql), eq(token))).thenReturn(0);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> verificationTokenDao.deleteByToken(token));
        assertFalse(exception.getMessage().contains("No token found"));
        verify(template).update(eq(sql), eq(token));
    }
}