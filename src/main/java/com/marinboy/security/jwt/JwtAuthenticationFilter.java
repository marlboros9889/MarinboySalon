package com.marinboy.security.jwt;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

/** Authorization Bearer 토큰의 서명·만료·Redis 폐기 여부를 검증합니다. */
@Profile("v3")
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenBlacklistService redisTokenBlacklistService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, RedisTokenBlacklistService redisTokenBlacklistService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTokenBlacklistService = redisTokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtTokenProvider.parseClaims(authorization.substring(7));
            if (redisTokenBlacklistService.isBlacklisted(claims.getId())) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "로그아웃된 토큰입니다.");
                return;
            }
            String role = claims.get("role", String.class);
            var authentication = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 토큰입니다.");
        }
    }
}
