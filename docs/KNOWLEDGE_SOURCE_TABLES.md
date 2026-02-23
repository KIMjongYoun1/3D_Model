# 지식 소스별 테이블 (한국은행, DART, 법령)

응답 형식에 맞춰 테이블을 나누고, **지식 활용** + **상세 목록 조회**를 지원합니다.

---

## 1. dart_corp_code (DART 기업코드)

- **용도**: DART `corpCode.json` 수집 결과. `list.json` 호출 시 `corp_code`를 넣으면 **검색 기간 3개월 제한 없음**.
- **수집**: `GET https://opendart.fss.or.kr/api/corpCode.json?crtfc_key=API_KEY`  
  (실제 제공은 ZIP/XML일 수 있음. JSON 엔드포인트가 있다면 동일 키로 호출)
- **컬럼**: corp_code(PK), corp_name, corp_name_eng, stock_code, modify_date, created_at

---

## 2. knowledge_bok (한국은행 ECOS)

- **용도**: StatisticSearch API 응답 형식 그대로 저장. 기준금리 등 경제 지표.
- **컬럼**: id, stat_code, stat_name, item_code1, item_name1, time, data_value, unit_name, created_at, updated_at
- **유니크**: (stat_code, item_code1, time)
- **상세 목록**: 시간순 조회, stat_code별 필터

---

## 3. knowledge_dart (DART 공시 목록)

- **용도**: `list.json` 응답 형식. corp_code 사용 시 장기 구간 검색 가능.
- **컬럼**: id, corp_code, corp_name, rcept_no(UNIQUE), rcept_dt, report_nm, flr_nm, rm, created_at, updated_at
- **상세 목록**: 접수일순, corp_code별 필터

---

## 4. knowledge_law (법령)

- **용도**: 국가법령정보센터 lawSearch 응답 형식.
- **컬럼**: id, mst(UNIQUE), law_name_ko, law_type, dept_name, proclamation_no, proclamation_date, enforce_date, law_id, content, source_url, created_at, updated_at
- **상세 목록**: 공포일순, 법령명/유형별 필터

---

## 5. 기존 knowledge_base

- 통합 지식(레거시) 또는 요약용으로 유지 가능.  
- 새 수집은 위 세 테이블(bok/dart/law) + dart_corp_code에 저장하고, 필요 시 knowledge_base에는 요약/연동만 저장하는 방식으로 전환 가능.

---

## API (Admin)

- `GET /api/admin/knowledge/bok` — 한국은행 목록 (최신순)
- `GET /api/admin/knowledge/bok/{id}` — 한국은행 상세
- `GET /api/admin/knowledge/dart` — DART 공시 목록
- `GET /api/admin/knowledge/dart/{id}` — DART 상세
- `GET /api/admin/knowledge/law` — 법령 목록
- `GET /api/admin/knowledge/law/{id}` — 법령 상세
- `GET /api/admin/knowledge/dart/corp-codes` — 기업코드 목록 (dart_corp_code)
