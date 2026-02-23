#!/usr/bin/env bash
#
# 한 서비스만 포그라운드로 기동 (에러 확인용)
# 사용: ./scripts/start-one.sh <서비스명>
#   서비스명: service | admin | python-ai | admin-ai | studio-fe | admin-fe
#
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

case "${1:-}" in
  service)
    echo "=== Java Service WAS (8080) - 포그라운드 (종료: Ctrl+C) ==="
    cd "$ROOT/backend-java/quantum-api-service" && ../mvnw spring-boot:run
    ;;
  admin)
    echo "=== Java Admin WAS (8081) - 포그라운드 (종료: Ctrl+C) ==="
    echo "  (선택) Service WAS(8080) 가 먼저 떠 있어야 합니다.)"
    cd "$ROOT/backend-java/quantum-api-admin" && ../mvnw spring-boot:run
    ;;
  python-ai)
    echo "=== Python AI Engine (8000) - 포그라운드 (종료: Ctrl+C) ==="
    cd "$ROOT/backend-python"
    [ -d "venv" ] && . venv/bin/activate
    uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
    ;;
  admin-ai)
    echo "=== Admin AI Server (8002) - 포그라운드 (종료: Ctrl+C) ==="
    cd "$ROOT/backend-admin-ai"
    [ -d "venv" ] && . venv/bin/activate
    uvicorn app.main:app --host 0.0.0.0 --port 8002 --reload
    ;;
  studio-fe)
    echo "=== Studio Frontend (3000) - 포그라운드 (종료: Ctrl+C) ==="
    cd "$ROOT/frontend-studio" && npm run dev
    ;;
  admin-fe)
    echo "=== Admin Frontend (3001) - 포그라운드 (종료: Ctrl+C) ==="
    cd "$ROOT/frontend-admin" && npm run dev
    ;;
  *)
    echo "사용법: $0 <서비스명>"
    echo ""
    echo "  서비스명:"
    echo "    service    - Java Service WAS (:8080)"
    echo "    admin      - Java Admin WAS (:8081)"
    echo "    python-ai  - Python AI Engine (:8000)"
    echo "    admin-ai   - Admin AI Server (:8002)"
    echo "    studio-fe  - Studio Frontend (:3000)"
    echo "    admin-fe   - Admin Frontend (:3001)"
    echo ""
    echo "  예: $0 service   ← Java만 기동해 보며 에러 확인"
    exit 1
    ;;
esac
