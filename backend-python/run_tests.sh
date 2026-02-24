#!/usr/bin/env bash
# Python 백엔드 단위 테스트 실행
# 사용법: ./run_tests.sh 또는 bash run_tests.sh

set -e
cd "$(dirname "$0")"

# pytest 설치 확인
if ! python3 -m pytest --version 2>/dev/null; then
  echo "pytest가 설치되어 있지 않습니다. 설치 중..."
  python3 -m pip install pytest -q
fi

echo "=== backend-python 테스트 실행 ==="
python3 -m pytest tests/ -v "$@"
