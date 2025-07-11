package com.app.Jwt;

import com.app.DTO.request.TokenRequest;
import io.jsonwebtoken.Claims;
import java.util.Date;
import java.util.function.Function;

public interface JwtProvider {
  String generateToken(TokenRequest request);

  String extractUsernameFromToken(String token);

  Date extractExpiration(String token);

  <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

  Boolean validateToken(String token, String username);
}
