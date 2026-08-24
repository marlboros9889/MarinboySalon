package com.marinboy.security.jwt;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import com.marinboy.dto.UserDto;

/** Authorization Bearer 토큰의 서명·만료·Redis 폐기 여부를 검증합니다. */
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
        //1. 공개 API처럼 토큰이 없는 요청은 인증을 만들지 않고 다음 필터로 전달합니다.
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            //2. 서명·만료를 검증한 뒤 로그아웃된 토큰인지 Redis에서 한 번 더 확인합니다.
            Claims claims = jwtTokenProvider.parseClaims(authorization.substring(7));
            if (redisTokenBlacklistService.isBlacklisted(claims.getId())) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "로그아웃된 토큰입니다.");
                return;
            }
            //3. 토큰 claim을 UserDto와 권한으로 복원해 컨트롤러가 같은 인증 정보를 사용하게 합니다.
            String role = claims.get("role", String.class);
            UserDto user = new UserDto();
            Number userId = claims.get("userId", Number.class);
            user.setId(userId == null ? null : userId.longValue());
            user.setUsername(claims.getSubject());
            user.setName(claims.get("name", String.class));
            user.setEmail(claims.get("email", String.class));
            user.setPhone(claims.get("phone", String.class));
            user.setRole(role);
            user.setLoginProvider(claims.get("loginProvider", String.class));
            var authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException exception) {
            // 잘못된 토큰이 이전 요청의 인증 정보로 이어지지 않도록 SecurityContext를 비웁니다.
            SecurityContextHolder.clearContext();
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 토큰입니다.");
        }
    }
}
