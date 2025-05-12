package com.app.Service;

import com.app.DAO.UserDao;
import com.app.DAO.VerificationTokenDao;
import com.app.Model.User;
import com.app.Model.VerificationToken;
import com.app.exception.sub.TokenExpireException;
import com.app.exception.sub.UserNotMatchException;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Objects;
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
        if (verificationToken == null) {
            throw new TokenExpireException("Verification token not found");
        }

        if (verificationToken.getExpireDate().toLocalDate().isBefore(LocalDate.now())) {
            throw new TokenExpireException("Verification token expired");
        }

        if (userDao.checkAccountVerifyById(verificationToken.getUserId())) {
            throw new TokenExpireException("User already verified");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user1 = userDao.findUserByUsername(authentication.getName());
        User user = userDao.findUserById(verificationToken.getUserId());

        if (!Objects.equals(user.getId(),user1.getId())) {
            throw new UserNotMatchException("Wrong user");
        }

        user.setEnable(true);
        userDao.update(user);
        verificationTokenDao.deleteByToken(token);
    }

    @Scheduled(cron = "0 0 * * * *")
    private void deleteExpireToken() {
        verificationTokenDao.deleteExpiredTokens();
    }
}
