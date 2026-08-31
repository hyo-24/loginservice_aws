package com.hyoju.member_service.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberJoinRequest {

    @NotBlank(message = "이름을 입력해주세요")
    private String name;

    @NotBlank(message = "아이디를 입력해주세요")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}
