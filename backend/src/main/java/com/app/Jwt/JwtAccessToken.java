package com.app.Jwt;

import com.app.DTO.request.TokenRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service(value = "jwtAccessToken")
public class JwtAccessToken implements JwtProvider {

    @Value("${ACCESS_TOKEN_KEY}")
    private String accessTokenKey;

    @Override
    public String generateToken(TokenRequest request){
        return createToken(request.getClaims(), request.getUsername());
    }

    private String createToken(Map<String,Object> claims,String username){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getSignKey(){
        byte[] keyBytes = Decoders.BASE64.decode(accessTokenKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String extractUsernameFromToken(String token){
        return extractClaim(token,Claims::getSubject);
    }

    @Override
    public Date extractExpiration(String token){
        return extractClaim(token,Claims::getExpiration);
    }

    @Override
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

    @Override
    public Boolean validateToken(String token, String username){
        final String usernameInToken = extractUsernameFromToken(token);
        return (username.equals(usernameInToken));
    }
}
