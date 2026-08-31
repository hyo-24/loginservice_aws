package com.hyoju.auth_service.web.control;

import com.hyoju.auth_service.service.AuthService;
import com.hyoju.auth_service.web.dto.LoginRequest;
import com.hyoju.auth_service.web.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService service;

    @PostMapping(value = "/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest login) {
        String token = service.login(login.getLoginId(), login.getPassword());
        LoginResponse loginResponse = new LoginResponse(token, "Bearer");
        return ResponseEntity.ok(loginResponse);
    }

}
