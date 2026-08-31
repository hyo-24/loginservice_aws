package com.hyoju.member_service.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCredentialsRequest {
    private String loginId;
    private String password;
}