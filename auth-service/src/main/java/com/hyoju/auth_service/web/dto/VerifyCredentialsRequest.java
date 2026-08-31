package com.hyoju.auth_service.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// member-service의 POST /internal/members/verify 요청 계약과 동일한 형태
@Getter
@AllArgsConstructor
public class VerifyCredentialsRequest {

    private String loginId;
    private String password;
}
