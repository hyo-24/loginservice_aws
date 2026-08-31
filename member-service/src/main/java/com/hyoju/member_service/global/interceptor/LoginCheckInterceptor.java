package com.hyoju.member_service.global.interceptor;

import com.hyoju.member_service.security.JwtValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor { // 인가 필터 만들기

    private final JwtValidator jwtValidator;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        // 인증 - 토큰 있는지 확인
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("인증 실패 헤더: {}", authHeader);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // 인증 - 토큰 유효한지 확인
        String token = authHeader.substring(7);
        if (!jwtValidator.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        Long memberId = jwtValidator.getMemberId(token);
        request.setAttribute("memberId", memberId);

        // 인가 - role 확인 ‼️
        String role = jwtValidator.getRole(token);
        if (!role.equals("ROLE_ADMIN")) {
            log.info("인가 실패 - role: {}", role);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }

        return true;
    }
}
