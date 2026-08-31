package com.hyoju.member_service.service;

import com.hyoju.member_service.domain.member.Member;
import com.hyoju.member_service.domain.member.MemberRepository;
import com.hyoju.member_service.web.dto.VerifyCredentialsResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    // DI 처럼 작동하게 하는 것 (스프링 사용 안하는 순수 자바코드라서 필요)
    @Mock // 가짜 객체 생성
    MemberRepository repository;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks // 가짜 객체를 이 객체에 DI
    MemberService service;

    @Test
    public void 회원가입() {
        // when
        service.join("kim", "hyo39", "hyo45");

        // then (반환값이 void)
        verify(repository, times(1)).save(any(Member.class));
    }

    @Test
    void 자격증명검증_성공() {
        // given
        Member member = new Member("kim", "3783", "2f83");
        BDDMockito.given(repository.findByLoginId("3783")).willReturn(Optional.of(member));
        BDDMockito.given(passwordEncoder.matches(any(), any())).willReturn(true);

        // when
        VerifyCredentialsResponse result = service.verifyCredentials("3783", "rawPassword");

        // then
        Assertions.assertThat(result.isValid()).isTrue();
        Assertions.assertThat(result.getRole()).isEqualTo("USER");
    }

    @Test
    void 자격증명검증_실패_존재하지않는회원() {
        // given
        BDDMockito.given(repository.findByLoginId("없는아이디"))
                .willReturn(Optional.empty());

        // when
        VerifyCredentialsResponse result = service.verifyCredentials("없는아이디", "1234");

        // then
        Assertions.assertThat(result.isValid()).isFalse();
    }

    @Test
    void 자격증명검증_실패_비밀번호불일치() {
        // given
        Member member = new Member("kim", "3783", "2f83");
        BDDMockito.given(repository.findByLoginId("3783")).willReturn(Optional.of(member));
        BDDMockito.given(passwordEncoder.matches(any(), any())).willReturn(false);

        // when
        VerifyCredentialsResponse result = service.verifyCredentials("3783", "wrongPassword");

        // then
        Assertions.assertThat(result.isValid()).isFalse();
    }

}
