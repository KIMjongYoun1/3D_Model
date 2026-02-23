#!/usr/bin/env bash
#
# Quantum Studio 전체 서비스 종료
#
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
exec "$SCRIPT_DIR/scripts/stop-all.sh"
