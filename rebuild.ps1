# =========================================
# 🚀 Script PowerShell: Full Clean + (Optional) Test + Rebuild All Services
# Chạy từ root folder chứa tất cả services
# ./build-all.ps1 -RunTests:$false/true
# =========================================

param (
    [bool]$RunTests = $true
)

# 1️⃣ Dừng và xóa tất cả Docker container
Write-Host "==> Stopping and removing all Docker containers..."
docker ps -aq | ForEach-Object { docker rm -f $_ }

# 2️⃣ Clean tất cả Maven + xóa dữ liệu test cũ
Write-Host "==> Cleaning all Maven target folders and old test reports..."
Get-ChildItem -Path . -Recurse -Filter "pom.xml" | ForEach-Object {
    $serviceFolder = Split-Path $_.FullName -Parent
    Write-Host "Cleaning $serviceFolder"

    mvn -f $serviceFolder clean
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Maven clean failed in $serviceFolder. Exiting."
        exit $LASTEXITCODE
    }

    # Xóa dữ liệu test cũ nếu còn sót
    $surefire = Join-Path $serviceFolder "target\surefire-reports"
    $jacoco = Join-Path $serviceFolder "target\jacoco.exec"
    $jacocoSite = Join-Path $serviceFolder "target\site\jacoco"

    if (Test-Path $surefire) { Remove-Item $surefire -Recurse -Force }
    if (Test-Path $jacoco) { Remove-Item $jacoco -Force }
    if (Test-Path $jacocoSite) { Remove-Item $jacocoSite -Recurse -Force }
}

# 3️⃣ Chạy Unit Test (nếu bật)
if ($RunTests) {
    Write-Host "==> Running unit tests..."
    Get-ChildItem -Path . -Recurse -Filter "pom.xml" | ForEach-Object {
        $serviceFolder = Split-Path $_.FullName -Parent
        Write-Host "Testing $serviceFolder"

        mvn -f $serviceFolder test
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Unit test failed in $serviceFolder. Exiting."
            exit $LASTEXITCODE
        }
    }
}
else {
    Write-Host "==> Skipping unit tests as requested."
}

# 4️⃣ Build tất cả services
Write-Host "==> Packaging all services..."
Get-ChildItem -Path . -Recurse -Filter "pom.xml" | ForEach-Object {
    $serviceFolder = Split-Path $_.FullName -Parent
    Write-Host "Building $serviceFolder"

    if ($RunTests) {
        mvn -f $serviceFolder package -DskipTests
    }
    else {
        mvn -f $serviceFolder package -DskipTests
    }

    if ($LASTEXITCODE -ne 0) {
        Write-Error "Build failed in $serviceFolder. Exiting."
        exit $LASTEXITCODE
    }
}

# 5️⃣ Docker Compose up (rebuild images)
Write-Host "==> Building and running Docker Compose..."
docker-compose up --build -d

Write-Host "==> Building and running Docker Compose (local)..."
docker-compose -f docker-compose.local.yml up --build -d

Write-Host "✅ All services rebuilt and running."
