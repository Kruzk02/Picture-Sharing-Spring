package com.app.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtProvider {

    @Value("${ACCESS_TOKEN_KEY}")
    private String accessTokenKey;

    @Value("${REFRESH_TOKEN_KEY}")
    private String refreshTokenKey;

    public Map<String, String> generateToken(String username, boolean isRemember){
        Map<String,Object> claims = new HashMap<>();
        Map<String, String> tokens = new HashMap<>();

        String accessToken = createAccessToken(claims,username);
        String refreshToken = createRefreshToken(username, isRemember);

        tokens.put("access-token", accessToken);
        tokens.put("refresh-token", refreshToken);

        return tokens;
    }

    private String createRefreshToken(String username, boolean isRemember) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date())
                .signWith(getSignRefreshTokenKey(), SignatureAlgorithm.HS512).compact();
    }

    private String createAccessToken(Map<String,Object> claims,String username){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .signWith(getSignAccessTokenKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getSignRefreshTokenKey() {
        byte[] keyBytes = Decoders.BASE64.decode(refreshTokenKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Key getSignAccessTokenKey(){
        byte[] keyBytes = Decoders.BASE64.decode(accessTokenKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsernameFromRefreshToken(String token) {
        return extractClaimFromRefreshToken()
    }

    public String extractUsernameFromAccessToken(String token){
        return extractClaimFromAccessToken(token,Claims::getSubject);
    }

    public Date extractExpiration(String token){
        return extractClaimFromAccessToken(token,Claims::getExpiration);
    }

    public <T> T extractClaimFromRefreshToken(String token, Function<Claims,T> claimsResolver) {
        final Claims claims = extractAllClaimsFromAccessToken(token);
        return claimsResolver.apply(claims);
    }

    public <T> T extractClaimFromAccessToken(String token, Function<Claims,T> claimsResolver){
        final Claims claims = extractAllClaimsFromAccessToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaimsFromRefreshToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignRefreshTokenKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Claims extractAllClaimsFromAccessToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignAccessTokenKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean validateToken(String token, UserDetails userDetails){
        final String username = extractUsernameFromAccessToken(token);
        return (username.equals(userDetails.getUsername()));
    }
}
