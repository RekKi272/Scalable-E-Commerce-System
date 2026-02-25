# =========================================
# 🚀 Script PowerShell: Clean + Test (optional) + Rebuild Single Service
# Sử dụng:
#   ./build-service.ps1 -ServiceName "product-service"
#   ./build-service.ps1 -ServiceName "product-service" -RunTests
# =========================================

param (
    [Parameter(Mandatory = $true)]
    [string]$ServiceName,

    [switch]$RunTests
)

# 1️⃣ Kiểm tra thư mục service có tồn tại không
$serviceFolder = Join-Path -Path (Get-Location) -ChildPath $ServiceName

if (-Not (Test-Path $serviceFolder)) {
    Write-Error "Folder '$ServiceName' does not exist. Exiting."
    exit 1
}

# 2️⃣ Clean Maven build
Write-Host "==> Cleaning Maven target for $ServiceName ..."
mvn -f $serviceFolder clean
if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven clean failed for $ServiceName. Exiting."
    exit $LASTEXITCODE
}

# 3️⃣ Run unit tests (chỉ khi bật cờ)
if ($RunTests) {
    Write-Host "==> Running unit tests for $ServiceName ..."
    mvn -f $serviceFolder test
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Unit tests failed for $ServiceName. Exiting."
        exit $LASTEXITCODE
    }
}
else {
    Write-Host "==> Skipping unit tests for $ServiceName ..."
}

# 4️⃣ Build Maven package
Write-Host "==> Building Maven package for $ServiceName ..."
mvn -f $serviceFolder package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven build failed for $ServiceName. Exiting."
    exit $LASTEXITCODE
}

# 5️⃣ Docker Compose build & up cho service này
Write-Host "==> Rebuilding and starting Docker for $ServiceName ..."
docker-compose build $ServiceName
docker-compose up -d $ServiceName

Write-Host "==> Rebuilding and starting Docker for $ServiceName (local)..."
docker-compose -f docker-compose.local.yml build $ServiceName
docker-compose -f docker-compose.local.yml up -d $ServiceName

Write-Host "✅ Service '$ServiceName' rebuilt and running."
