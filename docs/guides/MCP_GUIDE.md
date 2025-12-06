# MCP (Model Context Protocol) 가이드

Figma와 Notion을 Cursor에 연결하여 디자인부터 개발 문서화까지 원활하게 진행하는 가이드입니다.

## 목차
1. [빠른 시작 (5분)](#빠른-시작-5분)
2. [상세 설정](#상세-설정)
3. [사용법](#사용법)
4. [트러블슈팅](#트러블슈팅)

---

## 빠른 시작 (5분)

### 1단계: Figma Access Token 발급 (2분)
1. https://www.figma.com 로그인
2. 좌측 하단(또는 상단) **계정명 클릭** → **Settings**
3. 좌측 메뉴에서 **Personal access tokens** 클릭
4. **Create new token** 버튼 클릭
5. 토큰 이름 입력 (예: "Cursor MCP")
6. 토큰 복사 (⚠️ 한 번만 표시!)

### 2단계: Notion Integration 생성 (2분)
1. https://www.notion.so 로그인
2. **Settings & Members** → **Connections** → **Develop or manage integrations**
3. **New integration** 클릭
4. 이름: `Cursor MCP`
5. Capabilities 체크:
   - ✅ Read content
   - ✅ Insert content
   - ✅ Update content
6. **Submit** → **Internal Integration Token** 복사

### 3단계: 환경 변수 설정 (30초)
프로젝트 루트에 `.env` 파일 생성:
```bash
FIGMA_ACCESS_TOKEN=figd_여기에_토큰_붙여넣기
NOTION_API_KEY=secret_여기에_토큰_붙여넣기
```

### 4단계: MCP 설정 파일 생성 (30초)
프로젝트 루트에 `.cursor` 폴더 생성 후 `mcp.json` 파일 생성:
```json
{
  "mcpServers": {
    "figma": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-figma"
      ],
      "env": {
        "FIGMA_ACCESS_TOKEN": "${FIGMA_ACCESS_TOKEN}"
      }
    },
    "notion": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-notion"
      ],
      "env": {
        "NOTION_API_KEY": "${NOTION_API_KEY}"
      }
    }
  }
}
```

### 5단계: Cursor 재시작
1. Cursor 완전 종료
2. Cursor 재시작
3. 연결 테스트

---

## 상세 설정

### Figma MCP 설정

#### Figma Dev Mode MCP 서버 활성화
**방법 1: Figma 데스크톱 앱**
1. Figma 데스크톱 앱 실행
2. 상단 메뉴: Figma > Preferences (또는 Settings)
3. "Enable Dev Mode MCP Server" 옵션 체크
4. 재시작 (필요한 경우)

**방법 2: Figma 웹**
- Figma 웹 버전에서는 Dev Mode가 기본적으로 활성화되어 있습니다.
- MCP 서버는 데스크톱 앱에서만 사용 가능합니다.

#### Cursor 설정 파일 위치
- Windows: `%APPDATA%\Cursor\User\globalStorage\mcp.json`
- macOS: `~/Library/Application Support/Cursor/User/globalStorage/mcp.json`
- 또는 프로젝트 루트: `.cursor/mcp.json`

### Notion MCP 설정

#### Notion Integration 연결
1. Notion 페이지 열기
2. 우측 상단 "..." 메뉴 클릭
3. **"Connections"** 선택
4. 생성한 Integration 선택하여 연결

---

## 사용법

### Figma MCP 사용

**디자인 정보 가져오기:**
```
Figma 파일 [FILE_KEY]에서 노드 [NODE_ID]의 디자인 컨텍스트를 가져와줘
```

**디자인을 코드로 변환:**
```
Figma 디자인을 React/Next.js 컴포넌트로 변환해줘
- TypeScript 사용
- TailwindCSS 스타일링
```

**스크린샷 가져오기:**
```
Figma 파일 [FILE_KEY]에서 노드 [NODE_ID]의 스크린샷을 가져와줘
```

### Notion MCP 사용

**문서 검색:**
```
Notion에서 "프로젝트 문서" 검색해줘
```

**페이지 생성:**
```
Notion에 "새 문서" 페이지를 생성해줘
```

**페이지 업데이트:**
```
Notion 페이지 [PAGE_ID]의 내용을 업데이트해줘
```

---

## 트러블슈팅

### 일반적인 문제

#### 1. MCP 서버 연결 실패
**증상:** Cursor에서 MCP 도구를 사용할 수 없음

**해결 방법:**
- `.cursor/mcp.json` 파일이 올바른 위치에 있는지 확인
- 환경 변수가 `.env` 파일에 올바르게 설정되어 있는지 확인
- Cursor 재시작

#### 2. Figma MCP 작동 안 함
**증상:** Figma 디자인을 가져올 수 없음

**해결 방법:**
- Figma 데스크톱 앱이 실행 중인지 확인
- Figma Access Token이 유효한지 확인
- 파일 접근 권한 확인

#### 3. Notion MCP 파라미터 오류
**증상:** `Invalid arguments` 오류 발생

**해결 방법:**
- Notion API 파라미터 형식 확인
- Integration이 페이지에 연결되어 있는지 확인
- 수동 업로드 방법 사용 (참고: [NOTION_GUIDE.md](./NOTION_GUIDE.md))

#### 4. 환경 변수 로드 안 됨
**증상:** 환경 변수가 인식되지 않음

**해결 방법:**
- `.env` 파일이 프로젝트 루트에 있는지 확인
- 환경 변수 이름이 정확한지 확인
- Cursor 재시작

### JSON 문법 오류
**증상:** `mcp.json` 파일 오류

**해결 방법:**
- JSON 문법 확인 (온라인 JSON validator 사용)
- 따옴표, 쉼표 확인
- 들여쓰기 확인

---

## 참고 문서
- [NOTION_GUIDE.md](./NOTION_GUIDE.md): Notion 사용 가이드
- [FIGMA_GUIDE.md](../design/FIGMA_GUIDE.md): Figma 사용 가이드
- [WORKFLOW_DESIGN_TO_DOCS.md](./WORKFLOW_DESIGN_TO_DOCS.md): 전체 워크플로우

---

## 체크리스트

### Figma MCP 설정
- [ ] Figma 데스크톱 앱 설치
- [ ] Figma Dev Mode MCP 서버 활성화
- [ ] Figma Personal Access Token 발급
- [ ] `.env` 파일에 `FIGMA_ACCESS_TOKEN` 추가
- [ ] `.cursor/mcp.json`에 Figma 설정 추가
- [ ] Cursor 재시작
- [ ] 연결 테스트 성공

### Notion MCP 설정
- [ ] Notion Integration 생성
- [ ] Integration Token 복사
- [ ] `.env` 파일에 `NOTION_API_KEY` 추가
- [ ] `.cursor/mcp.json`에 Notion 설정 추가
- [ ] Notion 페이지에 Integration 연결
- [ ] Cursor 재시작
- [ ] 연결 테스트 성공

