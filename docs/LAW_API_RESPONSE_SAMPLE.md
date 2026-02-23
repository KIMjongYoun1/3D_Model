# 국가법령정보센터 API – 응답 샘플 (실제 호출 기준)

> **원칙**: [API_SAMPLE_FIRST_GUIDE.md](./API_SAMPLE_FIRST_GUIDE.md) — 먼저 curl로 호출해 JSON을 확인한 뒤, 그 구조에 맞춰 파싱 코드 작성.

---

## 1. 법령 검색 (target=law)

**curl**
```bash
curl -s "http://www.law.go.kr/DRF/lawSearch.do?OC=YOUR_OC&target=law&type=JSON&query=부가가치세법&display=10&page=1"
```

**쿼리 파라미터**
- `OC`: 기관코드(API 키)
- `target`: law
- `query`: 검색어 (URL 인코딩)
- `display`, `page`

**루트 키**: `LawSearch`

---

## 2. 전체 법령 목록 (target=lsStmd)

**curl**
```bash
curl -s "http://www.law.go.kr/DRF/lawSearch.do?OC=YOUR_OC&target=lsStmd&type=JSON&display=10&page=1"
```

**루트 키**: `LsStmdSearch` (LawSearch / lsStmd가 아님)

---

## 응답 구조 (target=law, query=부가가치세법)

```json
{
  "LawSearch": {
    "law": [
      {
        "현행연혁코드": "현행",
        "법령일련번호": "276117",
        "자법타법여부": "",
        "법령상세링크": "/DRF/lawService.do?OC=...&target=law&MST=276117&type=HTML&...",
        "법령명한글": "부가가치세법",
        "법령구분명": "법률",
        "소관부처명": "재정경제부",
        "공포번호": "21065",
        "제개정구분명": "타법개정",
        "소관부처코드": "1053000",
        "id": "1",
        "법령ID": "001571",
        "공동부령정보": "",
        "시행일자": "20260102",
        "공포일자": "20251001",
        "법령약칭명": ""
      },
      {
        "법령명한글": "부가가치세법 시행령",
        "법령구분명": "대통령령",
        "법령일련번호": "280855",
        ...
      },
      {
        "법령명한글": "부가가치세법 시행규칙",
        "법령구분명": "재정경제부령",
        "법령일련번호": "282645",
        ...
      }
    ],
    "resultMsg": "success",
    "키워드": "부가가치세법",
    "page": "1",
    "resultCode": "00",
    "target": "law",
    "totalCnt": "3",
    "section": "lawNm",
    "numOfRows": "3"
  }
}
```

---

## 항목별 필드 (DB 분류/매핑 참고)

| 한글 키 | 설명 | 저장/분류 활용 예 |
|--------|------|-------------------|
| **법령일련번호** | MST, 상세조회용 | `external_id` (항목 식별) |
| **법령명한글** | 법령명 | `title` |
| **법령구분명** | 법률 / 대통령령 / 부령 등 | 카테고리 세분화(LAW_TAX, LAW_ORDINANCE 등) |
| **소관부처명** | 재정경제부 등 | 메타/필터 |
| **공포일자** | YYYYMMDD | 메타, 정렬 |
| **시행일자** | YYYYMMDD | 메타 |
| **법령상세링크** | 상세 HTML/API 경로 | `source_url` 조합 |
| **현행연혁코드** | 현행/폐지 등 | is_active 등 |

**검색 결과 루트**
- `LawSearch.law`: 배열(또는 단일 객체)
- `LawSearch.totalCnt`: 총 건수
- `LawSearch.resultCode`: 00 성공

---

## 응답 구조 (target=lsStmd, 전체 법령 목록)

```json
{
  "LsStmdSearch": {
    "law": [
      {
        "id": "1",
        "법령ID": "010719",
        "법령일련번호": "253527",
        "법령명": "10ㆍ27법난 피해자의 명예회복 등에 관한 법률",
        "시행일자": "20230808",
        "법령구분명": "법률",
        "소관부처명": "문화체육관광부",
        "공포번호": "19592",
        "본문상세링크": "/DRF/lawService.do?OC=...&target=lsStmd&MST=253527&type=XML",
        "제개정구분명": "타법개정",
        "공포일자": "20230808",
        "소관부처코드": "1371000"
      }
    ],
    "resultMsg": "success",
    "page": "1",
    "resultCode": "00",
    "target": "lsStmd",
    "totalCnt": "5557",
    "section": "lawNm",
    "numOfRows": "10"
  }
}
```

**파싱 시 참고**
- `target=law` → 루트 `LawSearch`, 법령명 필드 `법령명한글`
- `target=lsStmd` → 루트 `LsStmdSearch`, 법령명 필드 `법령명` (법령명한글 없음)
- `parseLsStmdNode`에서 `법령명한글` 비면 `법령명`으로 fallback

---

## ---

## 3. 법령 상세 (target=law, MST로 조문 조회)

**curl**
```bash
curl -s "http://www.law.go.kr/DRF/lawService.do?OC=YOUR_OC&target=law&MST=276117&type=JSON"
```

**루트 키**: `법령`

**주요 구조**
- `법령.기본정보`: 법령명_한글, 공포일자, 시행일자 등
- `법령.조문.조문단위`: 조문 배열 (조문번호, 조문제목, 조문내용, 조문여부)
- `법령.부칙.부칙단위`: 부칙 내용
- `법령.개정문.개정문내용`: 개정 이력

**조문 저장 시**: 조문단위 전체를 순회하며 조문내용 이어붙임 (DB content 최대 ~30k자)

---

## DB 저장 시 공통 매핑

DB에 저장할 때는 `법령일련번호`로 항목 구분, `법령명`/`법령명한글`·`법령구분명`으로 제목/카테고리 분류.
