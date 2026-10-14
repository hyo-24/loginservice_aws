# JWT 기반 로그인 서비스 (MSA + CI/CD + IaC)

Spring Security 없이 JWT + Interceptor로 인증/인가를 직접 구현하고, MSA 구조로 재설계해 AWS에 배포한 뒤 CI/CD 자동화와 Terraform 기반 인프라 코드화까지 진행한 개인 프로젝트입니다.

## 프로젝트 배경

단일 Spring Boot 애플리케이션으로 시작한 로그인 서비스를, 신입 클라우드 엔지니어 채용 공고에서 자주 요구하는 두 가지 역량 — **MSA 아키텍처 이해**와 **CI/CD 배포 자동화 경험** — 을 갖추기 위해 재설계했습니다.

- 인증 로직과 회원 로직을 각각 독립된 서비스(auth-service, member-service)로 분리
- Docker 컨테이너화 후 AWS에 수동 배포 → GitHub Actions로 자동 배포까지 단계적으로 발전
- 배포 과정에서 발견한 보안 취약점(DB 비밀번호 평문 노출)을 AWS Secrets Manager로 개선

## 아키텍처

```
[GitHub main 브랜치에 push]
        │
        ▼
[GitHub Actions]
  ├─ 1. auth-service, member-service Docker 이미지 빌드
  ├─ 2. Docker Hub에 push
  └─ 3. EC2에 SSH 접속하여 배포 스크립트 실행
        │
        ▼
[EC2 인스턴스]
  ├─ AWS Secrets Manager에서 DB 비밀번호 조회 (IAM Role 인증)
  ├─ auth-service 컨테이너 (:8080)
  └─ member-service 컨테이너 (:8081)
        │
        ▼
[RDS (MySQL)]
```

**서비스 간 흐름**
1. 클라이언트가 auth-service로 로그인 요청
2. auth-service가 member-service에 내부 API로 회원 정보 검증 요청
3. 검증 성공 시 auth-service가 JWT 발급
4. 이후 요청은 Interceptor가 JWT를 검증하여 인가 처리

## 기술 스택

| 분류 | 스택 |
|---|---|
| Backend | Java, Spring Boot, JPA (Hibernate) |
| Database | MySQL (AWS RDS) |
| Infra | AWS EC2, RDS, VPC, IAM, Secrets Manager |
| Container | Docker |
| CI/CD | GitHub Actions |
| IaC | Terraform *(진행 중)* |

## 주요 기능 및 구현 포인트

### 인증/인가
- Spring Security 없이 JWT + `HandlerInterceptor`로 인증/인가 로직을 직접 구현
- 회원가입 시 BCrypt로 비밀번호 해싱
- JWT secret key는 코드에 하드코딩하지 않고 환경변수로 외부화
- 전역 예외 처리(`@ControllerAdvice`)로 일관된 에러 응답 제공

### MSA 설계
- 단일 서비스를 인증(auth-service)·회원(member-service)으로 분리
- 모노레포 구조로 관리 (하나의 저장소, 서비스별 폴더 분리)
- 서비스 간 내부 통신으로 인증 흐름 처리 (서버 분리가 아닌 서비스 단위 분리를 직접 적용)

### 배포 자동화 (CI/CD)
- GitHub Actions로 `main` 브랜치 push 시 자동 배포 파이프라인 구축
- 이미지 빌드 → Docker Hub push → EC2 SSH 접속 → 컨테이너 재배포까지 자동화
- job 간 의존성(`needs`)으로 빌드 완료 후 배포가 실행되도록 순서 제어

### 보안 개선
- 배포 자동화 과정에서 DB 비밀번호가 EC2 명령어 히스토리와 CI 설정에 평문으로 남는 문제를 발견
- AWS Secrets Manager + IAM Role(최소 권한 정책)을 도입하여, 배포 시점에 EC2가 직접 AWS로부터 인증받아 비밀번호를 조회하는 구조로 전환
- 특정 시크릿 하나만 조회 가능하도록 범위를 좁힌 커스텀 IAM 정책 적용

## 트러블슈팅

CI/CD 파이프라인 구축 과정에서 겪은 문제와 해결 과정은 아래 글에 자세히 정리했습니다.

- [GitHub Actions로 CI/CD 파이프라인 만들기 — 트러블슈팅 기록](#) *(벨로그 링크 추가 예정)*

## 실행 방법

### 로컬 실행
```bash
# auth-service
cd auth-service
./gradlew bootJar
docker build -t auth-service .
docker run -d -p 8080:8080 -e DB_PASSWORD=<비밀번호> auth-service

# member-service
cd member-service
./gradlew bootJar
docker build -t member-service .
docker run -d -p 8081:8081 -e DB_PASSWORD=<비밀번호> member-service
```

### 배포
`main` 브랜치에 push하면 GitHub Actions가 자동으로 빌드 및 EC2 배포를 수행합니다. (`.github/workflows/deploy.yml` 참고)

## 앞으로의 계획

- [ ] Terraform으로 현재 AWS 인프라(EC2, RDS, VPC, 보안 그룹) 코드화
- [ ] SSH 대신 AWS Systems Manager Session Manager로 EC2 접근 방식 개선
- [ ] Kubernetes를 이용한 컨테이너 오케스트레이션 학습 및 적용
- [ ] CloudWatch/Prometheus + Grafana를 통한 모니터링 구축
