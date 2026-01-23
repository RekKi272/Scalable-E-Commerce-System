# =========================================
# 🚀 FULL PIPELINE
# Clean → Unit Test → Build → Docker Deploy
# Chạy từ ROOT project
# =========================================

$services = @(
    "api-gateway",
    "auth-service",
    "service_registry",
    "blog-service",
    "cart-service",
    "order-service",
    "product-service",
    "payment-service",
    "user-service",
    "file-service",
    "notification-service"
)

$results = @()

Write-Host "====================================="
Write-Host "STEP 1: CLEAN ALL MODULES"
Write-Host "====================================="
mvn clean
if ($LASTEXITCODE -ne 0) {
    Write-Error "❌ Maven clean failed. Abort."
    exit 1
}

Write-Host ""
Write-Host "====================================="
Write-Host "STEP 2: RUN UNIT TESTS"
Write-Host "====================================="

foreach ($service in $services) {
    Write-Host ""
    Write-Host "-------------------------------------"
    Write-Host "Running unit test for: $service"
    Write-Host "-------------------------------------"

    mvn -pl $service -am test -DskipITs

    if ($LASTEXITCODE -eq 0) {
        $results += [PSCustomObject]@{
            Service = $service
            Result  = "PASS"
        }
        Write-Host "[PASS] $service"
    }
    else {
        $results += [PSCustomObject]@{
            Service = $service
            Result  = "FAIL"
        }
        Write-Host "[FAIL] $service"
    }
}

Write-Host ""
Write-Host "====================================="
Write-Host "UNIT TEST SUMMARY"
Write-Host "====================================="

$results | Format-Table -AutoSize

$failed = $results | Where-Object { $_.Result -eq "FAIL" }

if ($failed.Count -gt 0) {
    Write-Host ""
    Write-Host "❌ UNIT TEST FAILED – DEPLOY ABORTED"
    Write-Host "FAILED SERVICES:"
    $failed | Format-Table -AutoSize
    exit 1
}

Write-Host ""
Write-Host "✅ ALL SERVICES PASSED UNIT TESTS"

# =====================================================
# CHỈ CHẠY TỪ ĐÂY TRỞ ĐI KHI UNIT TEST OK
# =====================================================

Write-Host ""
Write-Host "====================================="
Write-Host "STEP 3: STOP & REMOVE OLD DOCKER"
Write-Host "====================================="

docker ps -aq | ForEach-Object { docker rm -f $_ }

Write-Host ""
Write-Host "====================================="
Write-Host "STEP 4: BUILD ALL SERVICES (SKIP TEST)"
Write-Host "====================================="

mvn package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "❌ Maven build failed. Abort Docker deploy."
    exit 1
}

Write-Host ""
Write-Host "====================================="
Write-Host "STEP 5: DOCKER COMPOSE BUILD & UP"
Write-Host "====================================="

docker-compose -f docker-compose.local.yml up --build -d
if ($LASTEXITCODE -ne 0) {
    Write-Error "❌ Docker compose failed."
    exit 1
}

Write-Host ""
Write-Host "====================================="
Write-Host "🎉 DEPLOY SUCCESS"
Write-Host "All services tested, built and deployed"
Write-Host "====================================="
exit 0
