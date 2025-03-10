package com.app.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
@AllArgsConstructor
public class JwtProvider {

    private static final String SECRET_KEY = "ePsWHptzmCNFlzdJsihQNxt2Ul0S+psp/S55LtUvKHE=";
    private final RedisTemplate<Object, Object> redisTemplate;

    public String generateToken(String username, boolean isRemember, boolean isVerify) {
        Map<String,Object> claims = new HashMap<>();
        claims.put("user-verification", isVerify);
        claims.put("remember-me", isRemember);

        String token = createToken(claims, username, isRemember);

        String key = "jwt:" + username;
        long ttl = isRemember ? TimeUnit.DAYS.toSeconds(7) : TimeUnit.MINUTES.toSeconds(30);

        redisTemplate.opsForValue().set(key, token, ttl, TimeUnit.SECONDS);
        return token;
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String createToken(Map<String,Object> claims,String username, boolean isRemember) {
        int expirationMin = isRemember ? 7 * 24 * 60 : 30;

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(Date.from(Instant.now().plus(expirationMin, ChronoUnit.MINUTES)))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getSignKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token,Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token,Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, String username) {
        String key = "jwt:" + username;
        String storedToken = (String) redisTemplate.opsForValue().get(key);

        return storedToken != null
                && storedToken.equals(token)
                && extractUsername(token).equals(username)
                && !isTokenExpired(token);
    }

    public boolean isTokenExpiringSoon(String token) {
        Date expiration = extractExpiration(token);
        long timeLeft = expiration.getTime() - System.currentTimeMillis();

        return timeLeft < TimeUnit.MINUTES.toMillis(5);
    }

    public String refreshToken(String username, boolean rememberMe, boolean isVerify) {
        return generateToken(username, rememberMe, isVerify);
    }

    public void refreshTokenTtlOnActivity(String username, boolean rememberMe) {
        String key = "jwt:" + username;
        Long currentTtl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        if (currentTtl > 0) {
            long newTtl = rememberMe ? TimeUnit.DAYS.toSeconds(7) : TimeUnit.MINUTES.toSeconds(15);
            redisTemplate.expire(key, newTtl, TimeUnit.SECONDS);
        }
    }
}
