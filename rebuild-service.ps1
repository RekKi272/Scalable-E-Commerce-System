# =========================================
# 🚀 Clean + Unit Test + Build + Docker (Single Service)
# Usage:
#   ./test-build-service.ps1 -ServiceName "product-service"
# =========================================

param (
    [Parameter(Mandatory = $true)]
    [string]$ServiceName
)

Write-Host "====================================="
Write-Host "SERVICE PIPELINE: $ServiceName"
Write-Host "====================================="

# 1️⃣ Kiểm tra thư mục service
$serviceFolder = Join-Path -Path (Get-Location) -ChildPath $ServiceName
if (-Not (Test-Path $serviceFolder)) {
    Write-Error "❌ Folder '$ServiceName' does not exist."
    exit 1
}

# 2️⃣ CLEAN
Write-Host ""
Write-Host "====================================="
Write-Host "STEP 1: CLEAN SERVICE"
Write-Host "====================================="
mvn -pl $ServiceName -am clean
if ($LASTEXITCODE -ne 0) {
    Write-Error "❌ Maven clean failed."
    exit 1
}

# 3️⃣ UNIT TEST
Write-Host ""
Write-Host "====================================="
Write-Host "STEP 2: RUN UNIT TEST"
Write-Host "====================================="
mvn -pl $ServiceName -am test -DskipITs

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "❌ UNIT TEST FAILED – STOP PIPELINE"
    Write-Host "[FAIL] $ServiceName"
    exit 1
}

Write-Host ""
Write-Host "✅ UNIT TEST PASSED"

# 4️⃣ BUILD (SKIP TEST)
Write-Host ""
Write-Host "====================================="
Write-Host "STEP 3: BUILD SERVICE (SKIP TEST)"
Write-Host "====================================="
mvn -f $serviceFolder package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "❌ Maven build failed."
    exit 1
}

# 5️⃣ DOCKER BUILD & UP (ONLY THIS SERVICE)
Write-Host ""
Write-Host "====================================="
Write-Host "STEP 4: DOCKER BUILD & RUN"
Write-Host "====================================="

docker-compose -f docker-compose.local.yml build $ServiceName
if ($LASTEXITCODE -ne 0) {
    Write-Error "❌ Docker build failed."
    exit 1
}

docker-compose -f docker-compose.local.yml up -d $ServiceName
if ($LASTEXITCODE -ne 0) {
    Write-Error "❌ Docker up failed."
    exit 1
}

Write-Host ""
Write-Host "====================================="
Write-Host "🎉 SERVICE READY"
Write-Host "$ServiceName tested, built and deployed"
Write-Host "====================================="
exit 0
