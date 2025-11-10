# Amall 프로젝트 설정 가이드

## 환경변수 설정

프로젝트를 실행하기 전에 환경변수를 설정해야 합니다.

### 방법 1: .env 파일 사용 (권장)

1. `.env.example` 파일을 복사하여 `.env` 파일 생성
```bash
cp .env.example .env
```

2. `.env` 파일 편집하여 실제 값 입력
```properties
DB_USERNAME=c##amall
DB_PASSWORD=your_actual_password
DB_URL=jdbc:oracle:thin:@192.168.1.104:1521:xe
```

### 방법 2: 시스템 환경변수 설정

#### Windows
```cmd
set DB_USERNAME=c##amall
set DB_PASSWORD=your_password
```

#### Mac/Linux
```bash
export DB_USERNAME=c##amall
export DB_PASSWORD=your_password
```

### 방법 3: IntelliJ IDEA에서 설정

1. Run → Edit Configurations
2. Environment variables에 추가:
   - `DB_USERNAME=c##amall`
   - `DB_PASSWORD=your_password`

## 프로젝트 실행

```bash
./gradlew bootRun
```

## 보안 주의사항

⚠️ **절대 커밋하지 말 것:**
- `.env` 파일
- `application-local.yml` 파일
- 실제 패스워드가 포함된 모든 파일

✅ **커밋해도 되는 것:**
- `.env.example` (예시 파일)
- `application.yml` (환경변수 참조만 포함)
