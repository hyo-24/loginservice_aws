package com.hyoju.auth_service.service;

import com.hyoju.auth_service.client.MemberServiceClient;
import com.hyoju.auth_service.security.JwtTokenProvider;
import com.hyoju.auth_service.web.dto.VerifyCredentialsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberServiceClient memberServiceClient;
    private final JwtTokenProvider jwtTokenProvider;

    // 로그인 기능 - 자격증명 검증은 member-service에 위임하고, 토큰 발급만 이 서비스가 담당
    public String login(String loginId, String password) {
        VerifyCredentialsResponse result = memberServiceClient.verifyCredentials(loginId, password);

        if (!result.isValid()) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenProvider.createToken(result.getMemberId(), result.getRole());
    }

}
