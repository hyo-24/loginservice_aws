# JWT 기반 로그인 서비스 (MSA + CI/CD + IaC + EKS)

Spring Security 없이 JWT + Interceptor로 인증/인가를 직접 구현하고, MSA 구조로 재설계해 AWS에 배포한 뒤 CI/CD 자동화, Terraform 기반 인프라 코드화, AWS EKS 배포까지 단계적으로 발전시킨 개인 프로젝트입니다.

## 프로젝트 배경

단일 Spring Boot 애플리케이션으로 시작한 로그인 서비스를, 신입 클라우드 엔지니어 채용 공고에서 자주 요구하는 두 가지 역량 — **MSA 아키텍처 이해**와 **CI/CD 배포 자동화 경험** — 을 갖추기 위해 재설계했습니다.

- 인증 로직과 회원 로직을 각각 독립된 서비스(auth-service, member-service)로 분리
- Docker 컨테이너화 후 AWS에 수동 배포 → GitHub Actions로 자동 배포까지 단계적으로 발전
- 배포 과정에서 발견한 보안 취약점(DB 비밀번호 평문 노출)을 AWS Secrets Manager로 개선
- EC2 수동 관리의 한계(컨테이너가 죽으면 직접 재실행, 콘솔로 만든 인프라를 지울 때 누락)를 느껴, Terraform으로 인프라를 코드화하고 AWS EKS로 전환

## 아키텍처

한 번에 EKS로 간 것이 아니라, 각 단계에서 불편했던 점을 다음 단계로 해결해왔습니다.

### 1차 — EC2 + CI/CD

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

**남은 문제** — 컨테이너가 죽으면 SSH로 들어가 직접 다시 띄워야 하고, 인스턴스가 한 대라 그 서버가 멈추면 서비스 전체가 멈춥니다.

### 2차 — Terraform + EKS

```
[GitHub Actions] ──빌드──▶ [Docker Hub]
                                │  각 노드의 kubelet이 pull
                                ▼
                     [ALB]  ← Ingress 규칙으로 경로 분기
                       │       /auth → auth, / → member, /internal → 403 차단
                       │       target-type: ip (NodePort 없이 Pod에 직접 연결)
                       ▼
     [EKS 클러스터 — 노드 2대, us-east-1a / us-east-1b]
       ├─ auth-service   Pod ×2 (:8080)
       └─ member-service Pod ×2 (:8081)
                       │
                       ▼
                 [RDS (MySQL)]
```

인프라는 Terraform으로 코드화해, VPC부터 클러스터·노드그룹·RDS·IAM까지 **22개 리소스를 한 번에 생성하고 한 번에 삭제**합니다.
인프라 코드와 배포 기록은 별도 저장소로 관리합니다. *(eks-infra 저장소 — 링크 추가 예정)*

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
| Infra | AWS EC2, EKS, RDS, VPC, IAM, Secrets Manager, ALB |
| Container | Docker, Kubernetes (AWS EKS) |
| CI/CD | GitHub Actions |
| IaC | Terraform (S3 원격 state) |

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

### 컨테이너 오케스트레이션 (EKS)
- Deployment로 Pod 개수를 선언해, Pod가 죽으면 선언한 개수를 자동으로 유지
- 노드를 두 개의 가용영역에 분산해 한쪽 장애를 견디도록 구성
- 서비스마다 로드밸런서를 두면 ALB가 두 개 필요해, Ingress로 경로를 분기해 **ALB 하나**로 처리 (비용 절감)
- `target-type: ip`로 ALB가 Pod IP에 직접 연결 — NodePort를 거치지 않고, 헬스체크가 노드가 아닌 Pod 단위로 동작
- 토큰 없이 호출되는 내부 전용 경로(`/internal/**`)는 ALB 단에서 고정 응답 403으로 차단
- 배포 후 회원가입 → 로그인 → 토큰 발급까지 한 번에 호출해 경로 분기·서비스 간 통신·클러스터 DNS·RDS 연결·JWT 발급을 함께 검증

### 보안 개선
- 배포 자동화 과정에서 DB 비밀번호가 EC2 명령어 히스토리와 CI 설정에 평문으로 남는 문제를 발견
- AWS Secrets Manager + IAM Role(최소 권한 정책)을 도입하여, 배포 시점에 EC2가 직접 AWS로부터 인증받아 비밀번호를 조회하는 구조로 전환
- 특정 시크릿 하나만 조회 가능하도록 범위를 좁힌 커스텀 IAM 정책 적용
- EKS에서는 Load Balancer Controller에 AWS 액세스 키를 심지 않고, IRSA로 서비스 계정이 IAM 역할을 위임받아 쓰도록 구성

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

- **이미지 빌드** — `main` 브랜치에 push하면 GitHub Actions가 두 서비스의 이미지를 빌드해 Docker Hub에 push합니다. (`.github/workflows/deploy.yml` 참고)
- **EC2 배포** — 같은 워크플로가 EC2에 SSH로 접속해 컨테이너를 재배포합니다.
- **EKS 배포** — 인프라는 `terraform apply`로 생성하고, 매니페스트 적용은 `kubectl`로 직접 수행합니다. 파이프라인에 포함하는 것은 남은 과제입니다.

## 앞으로의 계획

**완료**

- [x] Terraform으로 AWS 인프라 코드화 — VPC·EC2·RDS에서 시작해 EKS 클러스터·노드그룹·IAM·OIDC까지 22개 리소스
- [x] Kubernetes(AWS EKS)를 이용한 컨테이너 오케스트레이션 적용

**남은 것**

- [ ] CloudWatch 또는 Prometheus + Grafana를 통한 모니터링 구축
- [ ] readiness / liveness probe 추가
- [ ] External Secrets Operator 도입 — 현재 DB 비밀번호가 Terraform 변수·Secrets Manager·쿠버네티스 Secret 세 곳에 사본으로 존재하므로, Secrets Manager를 단일 원본으로 통합
- [ ] 사설 서브넷 분리 — 현재는 NAT 게이트웨이 비용 때문에 퍼블릭 서브넷만 사용하고, RDS는 `publicly_accessible = false` + 보안 그룹으로 차단
- [ ] EKS 매니페스트 적용까지 CI/CD 파이프라인에 포함
- [ ] SSH 대신 AWS Systems Manager Session Manager로 EC2 접근 방식 개선
