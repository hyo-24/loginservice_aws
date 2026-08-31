package com.hyoju.auth_service.client;

import com.hyoju.auth_service.web.dto.VerifyCredentialsRequest;
import com.hyoju.auth_service.web.dto.VerifyCredentialsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders; // 🌟 스프링 HttpHeaders 임포트 확인!
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class MemberServiceClient {

    private final RestTemplate restTemplate;

    @Value("${member-service.base-url}")
    private String memberServiceBaseUrl;

    public VerifyCredentialsResponse verifyCredentials(String loginId, String password) {
        VerifyCredentialsRequest request = new VerifyCredentialsRequest(loginId, password);

        // 1. "JSON 데이터로 보냅니다"라는 딱지(헤더) 만들기
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. 데이터와 딱지를 하나로 포장하기
        HttpEntity<VerifyCredentialsRequest> entity = new HttpEntity<>(request, headers);

        // 3. 포장한 상자(entity)로 전달하기
        return restTemplate.postForObject(
                memberServiceBaseUrl + "/internal/members/verify",
                entity,
                VerifyCredentialsResponse.class
        );
    }
}