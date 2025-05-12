package com.app.DAO;

import com.app.Model.VerificationToken;

public interface VerificationTokenDao {

    VerificationToken findByToken(String token);
    VerificationToken create(VerificationToken verificationToken);
    int deleteByToken(String token);
    void deleteExpiredTokens();

}
