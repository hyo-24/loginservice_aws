package com.hyoju.member_service.service;

import com.hyoju.member_service.domain.member.Member;
import com.hyoju.member_service.domain.member.MemberRepository;
import com.hyoju.member_service.web.dto.MemberInfoResponse;
import com.hyoju.member_service.web.dto.VerifyCredentialsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 기능
    public void join(String name, String loginId, String password) {
        Optional<Member> findMember = memberRepository.findByLoginId(loginId);

        // 중복 확인
        if (findMember.isPresent()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }

        String encodedPassword = passwordEncoder.encode(password); // 비밀번호 해시화

        Member member = new Member(name, loginId, encodedPassword);
        memberRepository.save(member); // 이때 자동으로 PK 값이 만들어져 같이 저장됨
    }

    // 자격증명 검증 - auth-service가 로그인 시 호출. 비밀번호 해시는 이 경계를 넘어 나가지 않는다.
    public VerifyCredentialsResponse verifyCredentials(String loginId, String password) {
        Optional<Member> findMember = memberRepository.findByLoginId(loginId);
        if (findMember.isEmpty()) {
            return VerifyCredentialsResponse.invalid();
        }

        Member member = findMember.get();
        if (!passwordEncoder.matches(password, member.getPassword())) { // 인자 순서 중요 ‼️ (평문,해시)
            return VerifyCredentialsResponse.invalid();
        }

        return VerifyCredentialsResponse.valid(member.getId(), member.getRole());
    }

    public MemberInfoResponse getMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return new MemberInfoResponse(member.getName(), member.getLoginId());
    }

}
