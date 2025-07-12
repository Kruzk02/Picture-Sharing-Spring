package com.app.Jwt;

import com.app.DTO.request.TokenRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.*;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service(value = "jwtAccessToken")
public class JwtAccessToken implements JwtProvider {

  @Value("${ACCESS_TOKEN_KEY}")
  private String accessTokenKey;

  @Override
  public String generateToken(TokenRequest request) {
    return createToken(request.getUsername());
  }

  private String createToken(String username) {
    return Jwts.builder()
        .header()
        .add("alg", "HS256")
        .type("JWT")
        .and()
        .subject(username)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
        .signWith(getSignKey())
        .compact();
  }

  private SecretKey getSignKey() {
    byte[] keyBytes = Decoders.BASE64.decode(accessTokenKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  @Override
  public String extractUsernameFromToken(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  @Override
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  @Override
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();
  }

  @Override
  public Boolean validateToken(String token, String username) {
    final String usernameInToken = extractUsernameFromToken(token);
    return (username.equals(usernameInToken));
  }
}
