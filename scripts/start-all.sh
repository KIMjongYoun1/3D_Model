#!/usr/bin/env bash
#
# 전체 서비스 기동 스크립트 (프로젝트 루트 또는 scripts/ 에서 실행)
# - Java Service WAS → Admin WAS → Python AI → Admin AI → Studio FE → Admin FE
# - 백엔드는 백그라운드, PID는 .run/pids 에 저장. 종료는 stop-all.sh 사용
# - 각 서비스 기동 로그는 .run/logs/ 에 저장, 콘솔에는 성공/실패 요약 출력
#
# 사용: ./scripts/start-all.sh [--tail|-t]
#   --tail, -t : 기동 후 로그 실시간 스트리밍 (Ctrl+C로 종료)
#
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

TAIL_LOGS=false
for arg in "$@"; do
  case "$arg" in
    --tail|-t) TAIL_LOGS=true ;;
  esac
done
RUN_DIR="$ROOT/.run"
PID_FILE="$RUN_DIR/pids"
LOG_DIR="$RUN_DIR/logs"
mkdir -p "$RUN_DIR" "$LOG_DIR"
touch "$LOG_DIR"/service.log "$LOG_DIR"/admin.log "$LOG_DIR"/python-ai.log "$LOG_DIR"/admin-ai.log "$LOG_DIR"/studio-fe.log "$LOG_DIR"/admin-fe.log 2>/dev/null || true

# 실행 시각 (로그 파일명·요약에 사용)
STARTED_AT=$(date '+%Y-%m-%d %H:%M:%S')
RUN_LOG="$LOG_DIR/start-all.log"

log_run() {
  echo "$1" | tee -a "$RUN_LOG"
}

# 기동 run 로그 초기화 (이번 실행만)
echo "=== start-all.sh $STARTED_AT (ROOT=$ROOT) ===" > "$RUN_LOG"

echo "=== Quantum Studio 전체 기동 (루트: $ROOT) ==="
log_run "시작 시각: $STARTED_AT"
log_run "  (특정 서비스만 기동/에러 확인: ./scripts/start-one.sh service | admin | python-ai | ...)"
log_run ""

# 포트 개방 대기 (최대 대기 초). 반환: 0=성공, 1=타임아웃
wait_for_port() {
  local port=$1
  local name=$2
  local max=${3:-90}
  local n=0
  while [ $n -lt $max ]; do
    if (nc -z 127.0.0.1 "$port" 2>/dev/null) || (curl -s -o /dev/null "http://127.0.0.1:${port}/" 2>/dev/null); then
      return 0
    fi
    n=$((n + 2))
    echo "  ... ${name} 기동 대기 (${port}) ${n}s" | tee -a "$RUN_LOG"
    sleep 2
  done
  return 1
}

# 포트가 열렸는지만 확인 (대기 없음)
check_port() {
  (nc -z 127.0.0.1 "$1" 2>/dev/null) || (curl -s -o /dev/null "http://127.0.0.1:$1/" 2>/dev/null)
}

save_pid() {
  echo "$1" >> "$PID_FILE"
}

: > "$PID_FILE"

S1=0 S2=0 S3=0 S4=0 S5=0 S6=0

# 0) Java 멀티 모듈 선빌드 (quantum-core → service, admin 의존성 해결)
log_run "[0/6] Java 백엔드 빌드 중..."
if (cd "$ROOT/backend-java" && ./mvnw clean install -DskipTests >> "$LOG_DIR/build.log" 2>&1); then
  log_run "  [성공] Java 빌드 완료"
else
  log_run "  [실패] Java 빌드 실패. 로그: $LOG_DIR/build.log"
  log_run "    수동 빌드: cd backend-java && ./mvnw clean install -DskipTests"
  log_run ""
  exit 1
fi
log_run ""

# 1) Java Service WAS (:8080)
log_run "[1/6] Java Service WAS (8080) 기동 중..."
(cd "$ROOT/backend-java/quantum-api-service" && ../mvnw spring-boot:run >> "$LOG_DIR/service.log" 2>&1) &
save_pid $!
if wait_for_port 8080 "Service WAS" 120; then
  S1=1; log_run "  [성공] Service WAS (8080)"
else
  log_run "  [실패] Service WAS (8080) - 타임아웃."
  log_run "    → 에러 확인: tail -f $LOG_DIR/service.log"
  log_run "    → Java만 직접 기동: ./scripts/start-one.sh service"
fi
log_run ""

# 2) Java Admin WAS (:8081)
log_run "[2/6] Java Admin WAS (8081) 기동 중..."
(cd "$ROOT/backend-java/quantum-api-admin" && ../mvnw spring-boot:run >> "$LOG_DIR/admin.log" 2>&1) &
save_pid $!
if wait_for_port 8081 "Admin WAS" 90; then
  S2=1; log_run "  [성공] Admin WAS (8081)"
else
  log_run "  [실패] Admin WAS (8081) - 타임아웃. tail -f $LOG_DIR/admin.log 또는 ./scripts/start-one.sh admin"
fi
log_run ""

# 2.5) Python venv 자동 생성 (없을 때만)
for pyproj in backend-python backend-admin-ai; do
  if [ ! -f "$ROOT/$pyproj/venv/bin/python" ] && [ -f "$ROOT/$pyproj/requirements.txt" ]; then
    log_run "[venv] $pyproj venv 생성 중..."
    (cd "$ROOT/$pyproj" && python3 -m venv venv && venv/bin/pip install -r requirements.txt -q) >> "$LOG_DIR/venv-$pyproj.log" 2>&1 && log_run "  [완료] $pyproj" || log_run "  [실패] $pyproj - 로그: $LOG_DIR/venv-$pyproj.log"
  fi
done
log_run ""

# 3) Python AI Engine (:8000)
log_run "[3/6] Python AI Engine (8000) 기동 중..."
(
  cd "$ROOT/backend-python"
  PYEXEC="python3"
  [ -f "venv/bin/python" ] && PYEXEC="venv/bin/python"
  exec "$PYEXEC" -m uvicorn app.main:app --host 0.0.0.0 --port 8000
) >> "$LOG_DIR/python-ai.log" 2>&1 &
save_pid $!
sleep 15
if check_port 8000; then S3=1; log_run "  [성공] Python AI Engine (8000)"; else log_run "  [실패] Python AI (8000) - 로그: $LOG_DIR/python-ai.log"; fi
log_run ""

# 4) Admin AI Server (:8002)
log_run "[4/6] Admin AI Server (8002) 기동 중..."
(
  cd "$ROOT/backend-admin-ai"
  PYEXEC="python3"
  [ -f "venv/bin/python" ] && PYEXEC="venv/bin/python"
  exec "$PYEXEC" -m uvicorn app.main:app --host 0.0.0.0 --port 8002
) >> "$LOG_DIR/admin-ai.log" 2>&1 &
save_pid $!
sleep 10
if check_port 8002; then S4=1; log_run "  [성공] Admin AI Server (8002)"; else log_run "  [실패] Admin AI (8002) - venv 생성: cd backend-admin-ai && python3 -m venv venv && pip install -r requirements.txt"; fi
log_run ""

# 5) Studio Frontend (:3000)
log_run "[5/6] Studio Frontend (3000) 기동 중..."
(cd "$ROOT/frontend-studio" && npm run dev >> "$LOG_DIR/studio-fe.log" 2>&1) &
save_pid $!
sleep 8
if check_port 3000; then S5=1; log_run "  [성공] Studio Frontend (3000)"; else log_run "  [실패] Studio FE (3000) - 로그: $LOG_DIR/studio-fe.log"; fi
log_run ""

# 6) Admin Frontend (:3001)
log_run "[6/6] Admin Frontend (3001) 기동 중..."
(cd "$ROOT/frontend-admin" && npm run dev >> "$LOG_DIR/admin-fe.log" 2>&1) &
save_pid $!
sleep 8
if check_port 3001; then S6=1; log_run "  [성공] Admin Frontend (3001)"; else log_run "  [실패] Admin FE (3001) - 로그: $LOG_DIR/admin-fe.log"; fi
log_run ""

# 요약
log_run "=== 기동 요약 ($STARTED_AT) ==="
log_run "  [1] Service WAS (8080)   : $([ $S1 -eq 1 ] && echo '성공' || echo '실패')"
log_run "  [2] Admin WAS (8081)     : $([ $S2 -eq 1 ] && echo '성공' || echo '실패')"
log_run "  [3] Python AI (8000)     : $([ $S3 -eq 1 ] && echo '성공' || echo '실패')"
log_run "  [4] Admin AI (8002)     : $([ $S4 -eq 1 ] && echo '성공' || echo '실패')"
log_run "  [5] Studio FE (3000)    : $([ $S5 -eq 1 ] && echo '성공' || echo '실패')"
log_run "  [6] Admin FE (3001)     : $([ $S6 -eq 1 ] && echo '성공' || echo '실패')"
log_run ""
log_run "  서비스별 상세 로그: $LOG_DIR/"
log_run "    service.log, admin.log, python-ai.log, admin-ai.log, studio-fe.log, admin-fe.log"
log_run "  이번 실행 로그: $RUN_LOG"
log_run "  종료: ./scripts/stop-all.sh"
log_run ""
log_run "  URL: Studio http://localhost:3000 | Admin http://localhost:3001"
log_run "       Service API :8080 | Admin API :8081 | Python AI :8000/docs | Admin AI :8002/health"
log_run ""

if [ "$TAIL_LOGS" = true ]; then
  echo ""
  echo "=== 로그 실시간 스트리밍 (종료: Ctrl+C) ==="
  echo "  .run/logs/ 내 서비스별 로그를 실시간으로 표시합니다."
  echo ""
  sleep 2
  tail -f "$LOG_DIR"/service.log "$LOG_DIR"/admin.log "$LOG_DIR"/python-ai.log "$LOG_DIR"/admin-ai.log "$LOG_DIR"/studio-fe.log "$LOG_DIR"/admin-fe.log 2>/dev/null || true
fi
