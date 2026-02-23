#!/usr/bin/env bash
#
# Quantum Studio 전체 기동 (프론트엔드 + 백엔드)
# - 6개 서비스 순차 기동, 로그는 .run/logs/ 에 저장
#
# 사용:
#   ./start.sh          # 전체 기동 (백그라운드)
#   ./start.sh --tail   # 기동 후 로그 실시간 스트리밍
#   ./start.sh -t       # 위와 동일
#
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
exec "$SCRIPT_DIR/scripts/start-all.sh" "$@"
