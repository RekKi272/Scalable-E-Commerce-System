# =========================================
# 🚀 Script PowerShell: Rebuild Maven + Docker
# =========================================

# Bước 1: Dừng và xóa tất cả Docker container đang chạy
# Lấy tất cả container (running & stopped) và xóa bắt buộc (-f)
docker ps -aq | ForEach-Object { docker rm -f $_ }

# Bước 2: Maven build project
# Chạy clean + package, bỏ qua test để build nhanh
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    # Nếu Maven build thất bại, thoát script với mã lỗi
    exit $LASTEXITCODE
}

# Bước 3: Docker Compose build & up
# Build lại image và chạy container
docker-compose up --build
