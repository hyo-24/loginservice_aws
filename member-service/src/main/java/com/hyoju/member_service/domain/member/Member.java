package com.hyoju.member_service.domain.member;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    // 클라이언트에게 받는 정보
    private String name;
    private String loginId;
    private String password;

    // 가입 시점에는 항상 USER로 고정 - ADMIN 전환은 DB에서 직접 수정
    private String role;

    // DB 의 PK값
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    public Member(String name, String loginId, String password) {
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.role = "USER";
    }


}
