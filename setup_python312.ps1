# Python 3.12 가상환경 설정 스크립트

Write-Host "=== Python 3.12 가상환경 설정 ===" -ForegroundColor Cyan
Write-Host ""

# Python 버전 확인
Write-Host "현재 Python 버전 확인 중..." -ForegroundColor Yellow
$pythonVersion = python --version 2>&1
Write-Host $pythonVersion -ForegroundColor Gray

if ($pythonVersion -notmatch "Python 3\.12") {
    Write-Host ""
    Write-Host "⚠️ 경고: Python 3.12가 아닙니다!" -ForegroundColor Yellow
    Write-Host "Python 3.12를 먼저 설치해주세요." -ForegroundColor Yellow
    Write-Host "다운로드: https://www.python.org/downloads/release/python-31211/" -ForegroundColor Cyan
    exit 1
}

Write-Host "✅ Python 3.12 확인됨" -ForegroundColor Green
Write-Host ""

# 기존 가상환경 삭제
if (Test-Path "venv") {
    Write-Host "기존 가상환경 삭제 중..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force "venv"
    Write-Host "✅ 기존 가상환경 삭제 완료" -ForegroundColor Green
    Write-Host ""
}

# 가상환경 생성
Write-Host "Python 3.12로 가상환경 생성 중..." -ForegroundColor Yellow
python -m venv venv

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 가상환경 생성 실패" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 가상환경 생성 완료" -ForegroundColor Green
Write-Host ""

# 가상환경 활성화
Write-Host "가상환경 활성화 중..." -ForegroundColor Yellow
& ".\venv\Scripts\Activate.ps1"

Write-Host "✅ 가상환경 활성화 완료" -ForegroundColor Green
Write-Host ""

# pip 업그레이드
Write-Host "pip 업그레이드 중..." -ForegroundColor Yellow
python -m pip install --upgrade pip setuptools wheel

Write-Host "✅ pip 업그레이드 완료" -ForegroundColor Green
Write-Host ""

# requirements.txt 설치
Write-Host "Python 패키지 설치 중..." -ForegroundColor Yellow
Write-Host "이 작업은 몇 분이 소요될 수 있습니다..." -ForegroundColor Gray
Write-Host ""

python -m pip install -r requirements.txt

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ 모든 패키지 설치 완료!" -ForegroundColor Green
    Write-Host ""
    Write-Host "설치된 패키지 확인:" -ForegroundColor Cyan
    python -m pip list
} else {
    Write-Host ""
    Write-Host "❌ 패키지 설치 중 오류 발생" -ForegroundColor Red
    Write-Host "오류 메시지를 확인해주세요." -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "=== 설정 완료 ===" -ForegroundColor Green
Write-Host ""
Write-Host "가상환경 활성화:" -ForegroundColor Cyan
Write-Host "  .\venv\Scripts\Activate.ps1" -ForegroundColor Gray
Write-Host ""
Write-Host "Python 버전 확인:" -ForegroundColor Cyan
Write-Host "  python --version" -ForegroundColor Gray

