package com.hyoju.member_service.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    Member save(Member member);

    // 회원 아이디로 조회 (로그인 할 때)
    Optional<Member> findByLoginId(String id);

}
