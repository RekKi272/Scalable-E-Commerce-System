# =========================================
# 🚀 Script PowerShell: Full Clean + Rebuild All Services
# Chạy từ root folder chứa tất cả services
# =========================================

# 1️⃣ Dừng và xóa tất cả Docker container
Write-Host "==> Stopping and removing all Docker containers..."
docker ps -aq | ForEach-Object { docker rm -f $_ }

# 2️⃣ (Tùy chọn) Xóa tất cả Docker image của project
# Nếu bạn muốn rebuild từ scratch, uncomment phần này
# Write-Host "==> Removing Docker images..."
# docker images -aq | ForEach-Object { docker rmi -f $_ }

# 3️⃣ Clean all Maven builds
Write-Host "==> Cleaning all Maven target folders..."
# Giả sử tất cả services đều có pom.xml ở folder con
Get-ChildItem -Path . -Recurse -Filter "pom.xml" | ForEach-Object {
    $serviceFolder = Split-Path $_.FullName -Parent
    Write-Host "Cleaning $serviceFolder"
    mvn -f $serviceFolder clean
}

# 4️⃣ Build all services with Maven (skip tests để nhanh)
Write-Host "==> Building all services..."
Get-ChildItem -Path . -Recurse -Filter "pom.xml" | ForEach-Object {
    $serviceFolder = Split-Path $_.FullName -Parent
    Write-Host "Building $serviceFolder"
    mvn -f $serviceFolder package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Build failed in $serviceFolder. Exiting."
        exit $LASTEXITCODE
    }
}

# 5️⃣ Docker Compose up (rebuild images)
Write-Host "==> Building and running Docker Compose (local)..."
docker-compose -f docker-compose.local.yml up --build -d

Write-Host "✅ All services rebuilt and running."
