package com.app.Jwt;

import com.app.Service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Qualifier(value = "jwtAccessToken") private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    public JwtAuthenticationFilter(JwtAccessToken jwtProvider, CustomUserDetailsService customUserDetailsService, RedisTemplate<Object, Object> redisTemplate) {
        this.jwtProvider = jwtProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        if(authHeader != null && authHeader.startsWith("Bearer ")){
            token = authHeader.substring(7);
            username = jwtProvider.extractUsernameFromToken(token);
        }

        if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if(authHeader != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            if(jwtProvider.validateToken(token,userDetails.getUsername())){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,null,userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request,response);
    }
}
