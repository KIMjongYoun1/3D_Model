#!/usr/bin/env bash
# 법령 상세 API(lawService.do) curl 테스트 – 본문(조문) 수신 확인
#
# 사용: ./scripts/test-law-detail-api.sh [OC] [MST]
#   OC: .env의 LAW_API_OC (기본 rkswlwhddbs1)
#   MST: 법령일련번호 (기본 276117=부가가치세법)
#
# 검증: 조문단위 배열 존재 시 본문 수신 가능

set -e
OC="${1:-rkswlwhddbs1}"
MST="${2:-276117}"
URL="http://www.law.go.kr/DRF/lawService.do?OC=${OC}&target=law&MST=${MST}&type=JSON"

echo "=== 법령 상세 API(lawService.do) 본문 수신 테스트 ==="
echo "URL: ${URL/OC=${OC}/OC=***}"
echo "MST: $MST (부가가치세법)"
echo ""

RESP=$(curl -s "$URL")
LEN=$(echo "$RESP" | wc -c | tr -d ' ')
echo "응답 길이: $LEN bytes"
echo ""

# 루트/구조 확인
if echo "$RESP" | grep -q '"법령"'; then
  echo "✓ 루트 키 '법령' 존재"
else
  echo "✗ 루트 키 '법령' 없음"
fi

if echo "$RESP" | grep -q '조문단위'; then
  echo "✓ 조문단위 배열 존재 (본문 수신 가능)"
  CNT=$(echo "$RESP" | grep -o '조문번호' | wc -l | tr -d ' ')
  echo "  조문 개수: 약 ${CNT}개"
else
  echo "✗ 조문단위 없음"
fi

# 조문 샘플 (제1조)
if echo "$RESP" | grep -q '제1조(목적)'; then
  echo "✓ 제1조(목적) 조문 내용 수신 확인"
  echo "$RESP" | grep -o '제1조(목적)[^"]*' | head -1 | cut -c1-120
  echo "..."
fi

# 저장
OUT=".run/law-detail-sample.json"
mkdir -p .run
echo "$RESP" > "$OUT"
echo ""
echo "전체 응답 저장: $OUT"
echo ""
echo "→ 법령 수집 시 본문이 DB에 저장되는지 Admin 지식 관리에서 확인하세요."
