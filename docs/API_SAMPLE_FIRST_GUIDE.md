# API 샘플 우선 개발 원칙 (Sample-First API Development)

> **핵심**: 외부 API 연동 시 **먼저 실제 호출로 JSON을 받아보고**, 그 구조에 맞춰 소스코딩한다.  
> 추측으로 키/구조를 정해두지 않는다.

---

## 개발 절차

```
1. curl / Postman 등으로 실제 API 호출
       ↓
2. 응답 JSON 구조 확인 (루트 키, 필드명, 배열/객체 형태)
       ↓
3. docs/*_RESPONSE_SAMPLE.md에 샘플 저장
       ↓
4. 소스코드 작성 (path(), get() 등 파싱 로직)
```

---

## 왜 필요한가

- **API 문서**와 **실제 응답**이 다른 경우가 있음 (예: LawSearch vs LsStmdSearch)
- `target`, `type` 등 파라미터에 따라 **응답 루트 키가 달라질 수 있음**
- 키가 `lawNm`인지 `법령명`인지 `법령명한글`인지는 **실제 응답을 봐야만 확실함**

---

## 적용 사례

### 국가법령정보센터 API
- `target=law` → 루트 키: `LawSearch`  
- `target=lsStmd` → 루트 키: `LsStmdSearch` (문서에는 없었으나 실제 curl로 확인)

→ `docs/LAW_API_RESPONSE_SAMPLE.md` 참고

### 향후 적용 대상
- DART API, 한국은행 ECOS API: 실제 호출 후 `*_RESPONSE_SAMPLE.md` 작성
- AI 에이전트 응답(Gemini/Ollama): `analyze_document` 실제 반환값 로깅 후 `mapping_service` 키 정합

---

## 샘플 문서 목록

| API | 샘플 문서 | curl 예시 |
|-----|----------|-----------|
| 국가법령정보센터 (target=law) | `LAW_API_RESPONSE_SAMPLE.md` | `lawSearch.do?target=law&query=부가가치세법` |
| 국가법령정보센터 (target=lsStmd) | `LAW_API_RESPONSE_SAMPLE.md` | `lawSearch.do?target=lsStmd&display=10&page=1` |
| DART | (미작성) | 실제 키로 호출 후 문서화 |
| 한국은행 ECOS | (미작성) | 실제 키로 호출 후 문서화 |

---

## 체크리스트 (신규 API 연동 시)

- [ ] curl/Postman으로 실제 호출
- [ ] 응답 JSON 전체 또는 일부를 `docs/*_RESPONSE_SAMPLE.md`에 저장
- [ ] 루트 키, 배열 경로, 필드명 정리
- [ ] 파싱 코드 작성 시 해당 샘플을 기준으로 `path("실제키")` 사용
- [ ] 샘플 문서에 curl 예시 명시
