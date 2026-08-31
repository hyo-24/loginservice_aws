package com.hyoju.member_service.web.control;

import com.hyoju.member_service.service.MemberService;
import com.hyoju.member_service.web.dto.MemberInfoResponse;
import com.hyoju.member_service.web.dto.MemberJoinRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService service;

    // 회원가입
    @PostMapping("/members/join")
    public String join(@RequestBody @Valid MemberJoinRequest request) {
        service.join(request.getName(), request.getLoginId(), request.getPassword());
        log.info("회원가입 요청 - loginId: {}", request.getLoginId());

        return "ok";
    }

    @GetMapping("/members/info")
    public MemberInfoResponse getMemberInfo(HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        return service.getMemberInfo(memberId);
    }

}
