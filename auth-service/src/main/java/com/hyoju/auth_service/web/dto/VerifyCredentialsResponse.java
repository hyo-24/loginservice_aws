package com.hyoju.auth_service.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// member-service의 POST /internal/members/verify 응답 계약과 동일한 형태
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCredentialsResponse {

    private boolean valid;
    private Long memberId;
    private String role;
}
