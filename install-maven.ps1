# Maven 설치 스크립트 (프로젝트 디렉토리용)

$mavenZip = Get-ChildItem -Path "$env:USERPROFILE\Downloads" -Filter "apache-maven-*.zip" -ErrorAction SilentlyContinue | 
            Sort-Object LastWriteTime -Descending | 
            Select-Object -First 1

if (-not $mavenZip) {
    Write-Host "다운로드 폴더에서 Maven zip 파일을 찾을 수 없습니다." -ForegroundColor Red
    Write-Host "다운로드 링크: https://dlcdn.apache.org/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.zip" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "zip 파일의 전체 경로를 입력하세요:" -ForegroundColor Cyan
    $mavenZip = Read-Host
    if (-not (Test-Path $mavenZip)) {
        Write-Host "파일을 찾을 수 없습니다: $mavenZip" -ForegroundColor Red
        exit 1
    }
}

$installPath = Join-Path $PSScriptRoot "tools\maven"
$extractPath = Join-Path $PSScriptRoot "tools"

Write-Host "Maven 설치 시작..." -ForegroundColor Cyan
Write-Host "압축 파일: $($mavenZip.FullName)" -ForegroundColor Gray
Write-Host "설치 경로: $installPath" -ForegroundColor Gray
Write-Host ""

# 기존 설치 제거
if (Test-Path $installPath) {
    Write-Host "기존 Maven 설치 제거 중..." -ForegroundColor Yellow
    Remove-Item -Path $installPath -Recurse -Force
}

# 압축 해제
Write-Host "압축 해제 중..." -ForegroundColor Yellow
Expand-Archive -Path $mavenZip.FullName -DestinationPath $extractPath -Force

# 압축 해제된 폴더 찾기
$extractedFolder = Get-ChildItem -Path $extractPath -Directory -Filter "apache-maven-*" | Select-Object -First 1

if ($extractedFolder) {
    # 폴더 이름을 maven으로 변경
    $finalPath = Join-Path $extractPath "maven"
    if (Test-Path $finalPath) {
        Remove-Item -Path $finalPath -Recurse -Force
    }
    Rename-Item -Path $extractedFolder.FullName -NewName "maven"
    Write-Host "압축 해제 완료!" -ForegroundColor Green
} else {
    Write-Host "압축 해제 실패: apache-maven 폴더를 찾을 수 없습니다." -ForegroundColor Red
    exit 1
}

# 환경 변수 설정 (현재 세션용)
$mavenHome = Join-Path $PSScriptRoot "tools\maven"
$mavenBin = Join-Path $mavenHome "bin"

# PATH에 추가 (현재 세션)
$env:Path = "$mavenBin;$env:Path"
$env:MAVEN_HOME = $mavenHome

Write-Host ""
Write-Host "=== Maven 설치 완료 ===" -ForegroundColor Green
Write-Host "설치 경로: $mavenHome" -ForegroundColor Gray
Write-Host ""

# Maven 버전 확인
Write-Host "Maven 버전 확인 중..." -ForegroundColor Cyan
try {
    & "$mavenBin\mvn.cmd" --version
    Write-Host ""
    Write-Host "✅ Maven 설치 성공!" -ForegroundColor Green
} catch {
    Write-Host "❌ Maven 실행 실패: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 중요: 영구 환경 변수 설정 ===" -ForegroundColor Yellow
Write-Host "현재 세션에서만 Maven이 사용 가능합니다." -ForegroundColor Gray
Write-Host "영구적으로 사용하려면 다음 환경 변수를 설정하세요:" -ForegroundColor Gray
Write-Host ""
Write-Host "MAVEN_HOME = $mavenHome" -ForegroundColor Cyan
Write-Host "Path에 추가: $mavenBin" -ForegroundColor Cyan
Write-Host ""
Write-Host "또는 프로젝트 루트에서 다음 명령어로 실행:" -ForegroundColor Gray
Write-Host "  .\tools\maven\bin\mvn.cmd --version" -ForegroundColor White

