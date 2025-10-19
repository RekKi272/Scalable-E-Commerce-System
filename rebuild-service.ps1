# =========================================
# 🚀 Script PowerShell: Clean + Rebuild Single Service
# Sử dụng: ./build-service.ps1 -ServiceName "product-service"
# =========================================

param (
    [Parameter(Mandatory = $true)]
    [string]$ServiceName
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

# 3️⃣ Build Maven package (skip tests)
Write-Host "==> Building Maven package for $ServiceName ..."
mvn -f $serviceFolder package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven build failed for $ServiceName. Exiting."
    exit $LASTEXITCODE
}

# 4️⃣ (Tùy chọn) Docker Compose build & up cho service này
Write-Host "==> Rebuilding and starting Docker for $ServiceName ..."
docker-compose build $ServiceName
docker-compose up -d $ServiceName

Write-Host "✅ Service '$ServiceName' rebuilt and running."
