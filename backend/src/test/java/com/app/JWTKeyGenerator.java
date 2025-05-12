package com.app;

import java.security.SecureRandom;
import java.util.Base64;

public class JWTKeyGenerator {
    public static void main(String[] args) {
        byte[] keyBytes = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(keyBytes);

        String secretKey = Base64.getEncoder().encodeToString(keyBytes);

        System.out.println("Generated JWT secret key: " + secretKey);
    }
}
