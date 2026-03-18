# UI 플로우 스크린샷 & 플로우차트

## 전체 프로젝트 상세 캡처 (Studio + Admin)

### 1. 프로젝트 기동

```bash
./start.sh
```

### 2. 전체 스크린샷 캡처

**로그인 세션 저장 + 캡처 (한 번에):**
```bash
cd frontend-studio
npm run screenshot-all:login
```
- 브라우저가 열리면 **Studio**: 네이버 로그인 → /studio 도착 시 세션 저장
- 이어서 **Admin**: 로그인 (ADMIN_EMAIL, ADMIN_PASSWORD 있으면 자동, 없으면 수동)
- 세션 저장 후 자동으로 캡처 진행

**세션 이미 있으면 그대로 캡처:**
```bash
npm run screenshot-all
```

- **Studio** (3000): home, login, studio, mypage, payment 등
- **Admin** (3001): login, dashboard, members, plans, terms 등
- **설정**: 1920x1080, fullPage, 2.5초 대기

### 3. 결과

| 경로 | 설명 |
|------|------|
| `docs/portfolio/screenshots/studio/` | Studio 페이지 PNG |
| `docs/portfolio/screenshots/admin/` | Admin 페이지 PNG |
| `docs/portfolio/ui-flow.html` | 플로우차트 (브라우저에서 열기) |

### 4. 플로우차트만 다시 생성

```bash
node scripts/generate-ui-flow.mjs
```

---

## 개별 앱만 캡처

```bash
cd frontend-studio
npm run screenshot-flow   # Studio만
```
