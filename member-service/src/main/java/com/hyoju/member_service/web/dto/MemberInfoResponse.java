package com.hyoju.member_service.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberInfoResponse {

    private String name;
    private String loginId;

}
