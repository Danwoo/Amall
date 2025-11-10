# Amall 프로젝트 보안 가이드

## Phase 1: 보안 개선 완료 사항

### 1. 패스워드 암호화
- ✅ **BCrypt 암호화** 적용
- ✅ 회원가입 시 자동 암호화
- ✅ 로그인 시 Spring Security가 자동 검증
- ✅ 회원정보 수정 시 암호화 처리

**구현 위치:**
- `MemberService.signUpMember()` - 회원가입 시 암호화
- `MemberService.updateMember()` - 정보 수정 시 암호화
- `SecurityConfig.passwordEncoder()` - BCrypt Bean 등록

---

### 2. Spring Security 적용
- ✅ 인증/인가 프레임워크 도입
- ✅ 폼 기반 로그인
- ✅ 세션 관리 (동시 세션 1개 제한)
- ✅ 자동 로그아웃

**구현 위치:**
- `SecurityConfig.java` - 전체 보안 설정
- `CustomUserDetailsService.java` - 사용자 인증
- `CustomAuthenticationSuccessHandler.java` - 로그인 성공 처리
- `CustomAuthenticationFailureHandler.java` - 로그인 실패 처리

**로그인 처리:**
```
POST /member/login
파라미터:
  - memberId: 사용자 ID
  - memberPwd: 패스워드
  - _csrf: CSRF 토큰 (자동)
```

---

### 3. XSS 방지
- ✅ ScriptUtils에 JavaScript 이스케이프 적용
- ✅ UTF-8 인코딩으로 변경
- ✅ javascript: 프로토콜 차단

**구현 위치:**
- `ScriptUtils.escapeJavaScript()` - XSS 방지 이스케이프

**보호되는 특수문자:**
- 백슬래시 `\`
- 따옴표 `'` `"`
- 개행 `\n` `\r`
- HTML 태그 `<` `>`
- 슬래시 `/`

---

### 4. CSRF 보호

#### CSRF란?
Cross-Site Request Forgery (사이트 간 요청 위조)
- 사용자가 의도하지 않은 요청을 공격자가 실행하는 공격
- Spring Security가 자동으로 방어

#### 현재 설정
```java
// SecurityConfig.java
.csrf(csrf -> csrf
    .ignoringRequestMatchers("/api/**")  // REST API는 제외
)
```

- ✅ **기본적으로 모든 POST, PUT, DELETE, PATCH 요청에 CSRF 토큰 필요**
- ✅ GET, HEAD, OPTIONS, TRACE는 토큰 불필요
- ✅ /api/** 경로는 CSRF 제외 (추후 REST API용)

#### Thymeleaf에서 CSRF 토큰 사용

**방법 1: 폼 자동 처리 (권장)**
```html
<form th:action="@{/signup/amall.com}" method="post">
    <!-- Spring Security가 자동으로 CSRF 토큰 hidden input 추가 -->
    <input type="text" name="memberId" />
    <input type="password" name="memberPwd" />
    <button type="submit">가입</button>
</form>
```

**방법 2: 수동 추가**
```html
<form action="/signup/amall.com" method="post">
    <!-- 수동으로 CSRF 토큰 추가 -->
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
    <input type="text" name="memberId" />
    <button type="submit">가입</button>
</form>
```

**방법 3: Ajax 요청**
```javascript
// 메타 태그에서 CSRF 토큰 가져오기
<meta name="_csrf" th:content="${_csrf.token}"/>
<meta name="_csrf_header" th:content="${_csrf.headerName}"/>

// JavaScript에서 사용
const token = document.querySelector('meta[name="_csrf"]').content;
const header = document.querySelector('meta[name="_csrf_header"]').content;

fetch('/api/data', {
    method: 'POST',
    headers: {
        [header]: token,
        'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
});
```

#### 주의사항

⚠️ **CSRF 토큰이 없으면 403 Forbidden 에러 발생**

다음 경로들은 CSRF 토큰 필요:
- `/signup/amall.com` (POST)
- `/member/login` (POST) - Spring Security가 자동 처리
- `/updateprofile/amall.com` (POST)
- `/sendMatching/amall.com` (POST)
- `/delete/amall.com` (POST)

다음 경로들은 CSRF 토큰 불필요:
- `/` (GET)
- `/member/login` (GET) - 로그인 페이지
- `/member/signUp` (GET) - 회원가입 페이지
- 모든 정적 리소스 (/css, /js, /images 등)

---

### 5. 환경변수 관리
- ✅ DB 패스워드 환경변수화
- ✅ .gitignore에 민감 정보 추가
- ✅ .env.example 제공

**설정 방법:** `SETUP.md` 참조

---

## 보안 체크리스트

### 완료된 항목 ✅
- [x] 패스워드 BCrypt 암호화
- [x] Spring Security 도입
- [x] XSS 방지 (ScriptUtils)
- [x] CSRF 보호 활성화
- [x] 환경변수 분리
- [x] .gitignore 설정
- [x] 세션 관리
- [x] UTF-8 인코딩

### Phase 2에서 개선할 항목 📋
- [ ] 서버 측 유효성 검증 강화
- [ ] 커스텀 예외 처리
- [ ] API 응답 표준화
- [ ] 로깅 체계 구축
- [ ] Rate Limiting (DDoS 방어)
- [ ] SQL Injection 재검증

---

## 참고 자료

### Spring Security 공식 문서
- https://docs.spring.io/spring-security/reference/

### OWASP Top 10
- https://owasp.org/www-project-top-ten/

### 보안 관련 이슈 발견 시
1. GitHub Issues에 보고
2. 심각한 보안 이슈는 비공개로 보고

---

**Last Updated:** Phase 1 완료 시점
**Security Level:** Basic → Intermediate
