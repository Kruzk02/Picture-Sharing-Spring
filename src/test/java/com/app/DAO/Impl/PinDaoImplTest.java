package com.app.DAO.Impl;

import com.app.Model.Pin;
import com.app.exception.sub.PinNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PinDaoImplTest {

    @Mock
    private JdbcTemplate template;

    @InjectMocks
    private PinDaoImpl pinDao;

    private Pin pin;

    @BeforeEach
    void setUp() {
        pin = Pin.builder()
                .id(1L)
                .description("NOPE")
                .fileName("YES")
                .image_url("/upload")
                .userId(1L)
                .build();
    }

    @Test
    void testGetAll() {
        String sql = "SELECT id,user_id,image_url FROM pins LIMIT 5 OFFSET ?";
        int offset = 5;

        List<Pin> expectedPins = new ArrayList<>();
        expectedPins.add(pin);

        when(template.query(eq(sql), any(RowMapper.class), eq(offset)))
                .thenReturn(expectedPins);

        pinDao.getAllPins(offset);

        verify(template).query(eq(sql), any(RowMapper.class), eq(offset));
    }

    @Test
    void testSave() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        long generatedId = 1L;
        keyHolder.getKeyList().add(Map.of("GENERATED_KEY", generatedId));

        when(template.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder passedKeyHolder = invocation.getArgument(1);
                    passedKeyHolder.getKeyList().add(Map.of("GENERATED_KEY", generatedId));
                    return 1;
                });


        Pin savedPin = pinDao.save(pin);

        assertNotNull(savedPin);
        assertEquals(generatedId, savedPin.getId());
        verify(template).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
    }

    @Test
    void testFindById() {
        String sql = "SELECT * FROM pins where id = ?";
        Long id = 1L;

        when(template.queryForObject(eq(sql), any(RowMapper.class), eq(id)))
                .thenReturn(pin);

        Pin result = pinDao.findById(id);

        assertNotNull(result);
        assertEquals(pin.getId(), result.getId());
        verify(template).queryForObject(eq(sql), any(RowMapper.class), eq(id));
    }

    @Test
    void testFindUserIdByPinId() {
        String sql = "SELECT id,user_id,image_url FROM pins where id = ?";
        Long id = 1L;

        when(template.queryForObject(eq(sql),any(RowMapper.class),eq(id))).thenReturn(pin);

        pinDao.findUserIdByPinId(id);
        verify(template).queryForObject(
                eq(sql),
                any(RowMapper.class),
                eq(id)
        );
    }

    @Test
    void testDeleteById() {
        String sql = "DELETE FROM pins WHERE id = ?";
        Long id = 1L;

        when(template.update(eq(sql),eq(id))).thenReturn(1);

        pinDao.deleteById(id);
        verify(template).update(eq(sql),eq(id));
    }
}