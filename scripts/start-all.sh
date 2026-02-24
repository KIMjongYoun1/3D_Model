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
# set -e: 모든 명령이 실패하면 즉시 스크립트 종료 (에러 전파)

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# 스크립트 위치 기준으로 프로젝트 루트 경로 계산 (scripts/start-all.sh → 3D_Model/)

cd "$ROOT"
# 작업 디렉터리를 프로젝트 루트로 이동

TAIL_LOGS=false
for arg in "$@"; do
  case "$arg" in
    --tail|-t) TAIL_LOGS=true ;;
  esac
done
# --tail 또는 -t 인수면 기동 후 로그 실시간 스트리밍 모드 활성화

RUN_DIR="$ROOT/.run"
PID_FILE="$RUN_DIR/pids"
LOG_DIR="$RUN_DIR/logs"
# .run/: 실행 중 PID·로그 저장 디렉터리
# pids: 프로세스 ID 목록 (stop-all.sh에서 종료 시 사용)
# logs/: 서비스별 로그 파일

mkdir -p "$RUN_DIR" "$LOG_DIR"
# .run/, .run/logs/ 디렉터리 생성 (없으면)

touch "$LOG_DIR"/service.log "$LOG_DIR"/admin.log "$LOG_DIR"/python-ai.log "$LOG_DIR"/admin-ai.log "$LOG_DIR"/studio-fe.log "$LOG_DIR"/admin-fe.log 2>/dev/null || true
# 각 서비스 로그 파일 미리 생성 (없으면). tail -f 시 에러 방지. 2>/dev/null: touch 실패 시 무시

# 실행 시각 (로그 파일명·요약에 사용)
STARTED_AT=$(date '+%Y-%m-%d %H:%M:%S')
RUN_LOG="$LOG_DIR/start-all.log"
# 이번 실행의 요약 로그 파일 경로

log_run() {
  echo "$1" | tee -a "$RUN_LOG"
}
# tee -a: 콘솔 출력 + RUN_LOG 파일에 추가

# 기동 run 로그 초기화 (이번 실행만)
echo "=== start-all.sh $STARTED_AT (ROOT=$ROOT) ===" > "$RUN_LOG"
# > : 덮어쓰기. 이전 실행 기록 초기화

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
# nc -z: 포트 열림 여부 확인. curl: HTTP로 404 등도 응답이면 포트 열림으로 간주
# 2초마다 체크, max초 동안 대기. Java 서비스는 기동이 느려서 사용

# 포트가 열렸는지만 확인 (대기 없음)
check_port() {
  (nc -z 127.0.0.1 "$1" 2>/dev/null) || (curl -s -o /dev/null "http://127.0.0.1:$1/" 2>/dev/null)
}
# Python/Node는 2초 내 기동 가능하므로 대기 없이 한 번만 확인

save_pid() {
  echo "$1" >> "$PID_FILE"
}
# 백그라운드 프로세스 PID를 $PID_FILE에 추가 (stop-all.sh에서 kill 시 사용)

: > "$PID_FILE"
# : (빈 명령): true와 동일. > "$PID_FILE"로 파일 내용 비우기 (이전 PID 초기화)

S1=0 S2=0 S3=0 S4=0 S5=0 S6=0
# 각 서비스 기동 성공 여부: 1=성공, 0=실패
# S1=Service WAS, S2=Admin WAS, S3=Python AI, S4=Admin AI, S5=Studio FE, S6=Admin FE

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
# mvnw: Maven Wrapper. clean install: 기존 빌드 삭제 후 전체 빌드
# -DskipTests: 테스트 제외 (빌드 속도). >> build.log: 출력을 로그 파일로 리다이렉트
# 2>&1: stderr를 stdout으로 합쳐서 함께 리다이렉트

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
# ( ... ) &: 서브셸에서 실행 + 백그라운드(&). $!: 방금 실행한 프로세스의 PID
# spring-boot:run: Maven 플러그인으로 Spring Boot 앱 실행
# wait_for_port 8080 ... 120: 8080 포트가 열릴 때까지 최대 120초 대기

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
# venv: Python 가상환경. venv/bin/python 없고 requirements.txt 있으면 venv 생성
# python3 -m venv venv: venv 디렉터리에 가상환경 생성
# pip install -r requirements.txt -q: 의존성 설치 (-q: quiet, 출력 최소화)

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
# uvicorn: FastAPI ASGI 서버. app.main:app: FastAPI 앱 인스턴스
# --host 0.0.0.0: 외부 접속 허용. sleep 15: Python 기동 대기 시간

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

# 4.5) Frontend npm install (recharts 등 새 의존성 반영)
for fe in frontend-studio frontend-admin; do
  if [ -f "$ROOT/$fe/package.json" ]; then
    log_run "[npm] $fe 의존성 설치 중..."
    (cd "$ROOT/$fe" && npm install >> "$LOG_DIR/npm-$fe.log" 2>&1) && log_run "  [완료] $fe" || log_run "  [경고] $fe npm install 실패 - 기존 node_modules 사용"
  fi
done
log_run ""

# 5) Studio Frontend (:3000)
log_run "[5/6] Studio Frontend (3000) 기동 중..."
(cd "$ROOT/frontend-studio" && npm run dev >> "$LOG_DIR/studio-fe.log" 2>&1) &
save_pid $!
sleep 8
if check_port 3000; then S5=1; log_run "  [성공] Studio Frontend (3000)"; else log_run "  [실패] Studio FE (3000) - 로그: $LOG_DIR/studio-fe.log"; fi
log_run ""
# npm run dev: Next.js 개발 서버. sleep 8: Node 기동 대기

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
# tail -f: 파일 끝을 실시간으로 출력 (추가되는 로그 계속 표시)
# --tail 옵션으로 실행 시 기동 후 모든 서비스 로그를 한 화면에 스트리밍
