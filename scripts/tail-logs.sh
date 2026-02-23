#!/usr/bin/env bash
#
# 기동 중인 서비스 로그 실시간 확인
# (start-all.sh 로 기동한 후 별도 터미널에서 실행)
#
# 사용: ./scripts/tail-logs.sh [서비스명]
#   서비스명 생략 시 전체 로그 스트리밍
#   서비스명: service | admin | python-ai | admin-ai | studio-fe | admin-fe
#
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$ROOT/.run/logs"

if [ ! -d "$LOG_DIR" ]; then
  echo "로그 디렉토리가 없습니다. 먼저 ./start.sh 로 기동하세요."
  exit 1
fi

case "${1:-}" in
  service)    tail -f "$LOG_DIR/service.log" ;;
  admin)      tail -f "$LOG_DIR/admin.log" ;;
  python-ai)  tail -f "$LOG_DIR/python-ai.log" ;;
  admin-ai)   tail -f "$LOG_DIR/admin-ai.log" ;;
  studio-fe)  tail -f "$LOG_DIR/studio-fe.log" ;;
  admin-fe)   tail -f "$LOG_DIR/admin-fe.log" ;;
  *)
    echo "=== 전체 로그 스트리밍 (종료: Ctrl+C) ==="
    tail -f "$LOG_DIR"/service.log "$LOG_DIR"/admin.log "$LOG_DIR"/python-ai.log \
            "$LOG_DIR"/admin-ai.log "$LOG_DIR"/studio-fe.log "$LOG_DIR"/admin-fe.log 2>/dev/null || echo "로그 파일 없음"
    ;;
esac
