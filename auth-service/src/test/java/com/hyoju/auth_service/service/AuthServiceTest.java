package com.hyoju.auth_service.service;

import com.hyoju.auth_service.client.MemberServiceClient;
import com.hyoju.auth_service.security.JwtTokenProvider;
import com.hyoju.auth_service.web.dto.VerifyCredentialsResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    MemberServiceClient memberServiceClient;
    @Mock
    JwtTokenProvider jwtTokenProvider;
    @InjectMocks
    AuthService service;

    @Test
    void 로그인_성공() {
        // given
        VerifyCredentialsResponse valid = new VerifyCredentialsResponse(true, 1L, "USER");
        BDDMockito.given(memberServiceClient.verifyCredentials("3783", "1234")).willReturn(valid);
        BDDMockito.given(jwtTokenProvider.createToken(1L, "USER")).willReturn("testToken");

        // when
        String token = service.login("3783", "1234");

        // then
        Assertions.assertThat(token).isEqualTo("testToken");
    }

    @Test
    void 로그인_실패_자격증명불일치() {
        // given
        VerifyCredentialsResponse invalid = new VerifyCredentialsResponse(false, null, null);
        BDDMockito.given(memberServiceClient.verifyCredentials(any(), any())).willReturn(invalid);

        // then
        assertThatThrownBy(() -> service.login("3783", "wrongPassword"))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
