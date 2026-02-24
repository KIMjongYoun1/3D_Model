#!/usr/bin/env bash
# 프로젝트 .env의 GEMINI_API_KEY로 Gemini API 호출 테스트
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# .env 로드 (GEMINI_API_KEY)
if [ -f .env ]; then
  set -a
  source .env 2>/dev/null || true
  set +a
fi
# 또는 env.example에서 복사한 경우
[ -z "$GEMINI_API_KEY" ] && [ -f .env.local ] && set -a && source .env.local 2>/dev/null && set +a

if [ -z "$GEMINI_API_KEY" ]; then
  echo "❌ GEMINI_API_KEY가 .env에 없습니다."
  exit 1
fi

echo "🔗 Gemini API 호출 테스트 (Key: ${GEMINI_API_KEY:0:10}...)"
echo ""

# gemini-2.5-flash (무료 tier)
curl -s -X POST \
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$GEMINI_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "contents": [{
      "parts": [{"text": "이 메시지가 보인다면 API 연결 성공이라고 한 단어로 답해줘."}]
    }]
  }' | python3 -c "
import sys, json
try:
    d = json.load(sys.stdin)
    if 'candidates' in d and d['candidates']:
        text = d['candidates'][0]['content']['parts'][0]['text']
        print('='*40)
        print('✅ Gemini API 호출 성공!')
        print(f'🤖 응답: {text.strip()}')
        print('='*40)
    elif 'error' in d:
        print('❌ API 에러:', d['error'].get('message', d['error']))
        sys.exit(1)
    else:
        print('❌ 예상치 못한 응답:', d)
        sys.exit(1)
except Exception as e:
    print('❌ 파싱 실패:', e)
    sys.exit(1)
"
