package com.hyoju.member_service.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // 🌟 필수 추가 (Jackson 역직렬화용)
@AllArgsConstructor
public class VerifyCredentialsResponse {

    private boolean valid;
    private Long memberId;
    private String role;

    public static VerifyCredentialsResponse invalid() {
        return new VerifyCredentialsResponse(false, null, null);
    }

    public static VerifyCredentialsResponse valid(Long memberId, String role) {
        return new VerifyCredentialsResponse(true, memberId, role);
    }
}