package com.app.Service;

import com.app.DAO.UserDao;
import com.app.DAO.VerificationTokenDao;
import com.app.Model.User;
import com.app.Model.VerificationToken;
import com.app.exception.sub.TokenExpireException;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@AllArgsConstructor
public class VerificationTokenService {

    private final VerificationTokenDao verificationTokenDao;
    private final UserDao userDao;

    public VerificationToken generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .userId(user.getId())
                .expireDate(Date.valueOf(LocalDate.now().plusDays(1)))
                .build();
        return verificationTokenDao.create(verificationToken);
    }

    public void verifyAccount(String token) {
        VerificationToken verificationToken = verificationTokenDao.findByToken(token);
        if (verificationToken != null && !verificationToken.getExpireDate().toLocalDate().isBefore(LocalDate.now())) {
            User user = userDao.findUserById(verificationToken.getUserId());
            user.setEnable(true);
            userDao.update(user);
            verificationTokenDao.deleteByToken(token);
        } else {
            throw new TokenExpireException("Verification token expired");
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    private void deleteExpireToken() {
        verificationTokenDao.deleteExpiredTokens();
    }
}
