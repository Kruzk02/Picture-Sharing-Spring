package com.app.DAO.Impl;

import com.app.DAO.VerificationTokenDao;
import com.app.Model.VerificationToken;
import com.app.exception.sub.VerificationTokenNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

@AllArgsConstructor
@Repository
public class VerificationTokenDaoImpl implements VerificationTokenDao {

    private final JdbcTemplate template;

    @Override
    @Transactional(readOnly = true)
    public VerificationToken findByToken(String token) {
        try {
            String sql = "SELECT token, user_id, expiration_date FROM verification_token WHERE token = ?";
            return template.queryForObject(sql,
                (rs, rowNum) -> VerificationToken.builder()
                    .token(rs.getString("token"))
                    .userId(rs.getLong("user_id"))
                    .expireDate(rs.getDate("expiration_date"))
                    .build()
                ,token);
        } catch (EmptyResultDataAccessException e) {
            throw new VerificationTokenNotFoundException("Verification token not found");
        }
    }

    @Override
    @Transactional
    public VerificationToken create(VerificationToken verificationToken) {
        String sql = "INSERT INTO verification_token(token, user_id, expiration_date) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rows = template.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, verificationToken.getToken());
            ps.setLong(2, verificationToken.getUserId());
            ps.setDate(3, verificationToken.getExpireDate());
            return ps;
        }, keyHolder);

        if (rows > 0) {
            verificationToken.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
            return verificationToken;
        }
        throw new DataAccessException("Failed to create verification token") {};
    }

    @Override
    @Transactional
    public int deleteByToken(String token) {
        String sql = "DELETE FROM verification_token WHERE token = ?";
        int rowsAffected = template.update(sql, token);

        if (rowsAffected == 0) {
            throw new VerificationTokenNotFoundException("No records found to delete for the provided token or expired entries.");
        }

        return rowsAffected;
    }

    @Override
    public void deleteExpiredTokens() {
        String sql = "DELETE FROM verification_token WHERE expiration_date < NOW()";
        int rowsDeleted = template.update(sql);
        System.out.println(rowsDeleted + " expired tokens deleted.");
    }
}
