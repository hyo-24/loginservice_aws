package com.hyoju.auth_service.web.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginRequest { // 로그인 요청 DTO

    private String loginId;
    private String password;
}
