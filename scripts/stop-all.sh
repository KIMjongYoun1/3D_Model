#!/usr/bin/env bash
#
# start-all.sh 로 기동한 전체 서비스 종료
# 1) .run/pids 에 저장된 PID kill
# 2) 해당 포트 사용 중인 프로세스 추가 정리 (8080, 8081, 8000, 8002, 3000, 3001)
#
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PID_FILE="$ROOT/.run/pids"
PORTS="8080 8081 8000 8002 3000 3001"

echo "=== 기동 중인 서비스 종료 ==="

if [ -f "$PID_FILE" ]; then
  while read -r pid; do
    [ -z "$pid" ] && continue
    if kill -0 "$pid" 2>/dev/null; then
      echo "  kill PID $pid"
      kill "$pid" 2>/dev/null || true
    fi
  done < "$PID_FILE"
  rm -f "$PID_FILE"
fi

# 포트 사용 중인 프로세스 종료 (macOS: lsof -ti :PORT)
for port in $PORTS; do
  pid=$(lsof -ti ":$port" 2>/dev/null)
  if [ -n "$pid" ]; then
    echo "  kill port $port (PID $pid)"
    kill $pid 2>/dev/null || true
  fi
done

echo "종료 완료."
