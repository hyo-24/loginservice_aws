package com.hyoju.member_service.web.control;

import com.hyoju.member_service.service.MemberService;
import com.hyoju.member_service.web.dto.VerifyCredentialsRequest;
import com.hyoju.member_service.web.dto.VerifyCredentialsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// auth-service 전용 내부 API. 운영 환경에서는 내부망/서비스 메시로만 접근 가능하도록 제한해야 한다.
@RestController
@RequiredArgsConstructor
public class InternalMemberController {

    private final MemberService service;

    @PostMapping("/internal/members/verify")
    public VerifyCredentialsResponse verify(@RequestBody VerifyCredentialsRequest request) {
        return service.verifyCredentials(request.getLoginId(), request.getPassword());
    }

}
