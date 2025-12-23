# Quick Start Guide - 3 Steps to Run

## Step 1: Navigate to Project
```bash
cd health-check-springboot
```

## Step 2: Run the Application
```bash
mvn spring-boot:run
```

**Wait for:**
```
✅ Health Check Service initialized - Monitoring 3 endpoints
Started HealthCheckApplication in X seconds
```

## Step 3: Test the Endpoints

### Test 1: Check Health
```bash
curl http://localhost:8080/health
```

**Expected Output:**
```json
{
  "overallStatus": "HEALTHY",
  "checks": [
    {
      "endpoint": "service-1",
      "status": "UP",
      "responseTime": 145,
      "statusCode": 200,
      "timestamp": "..."
    },
    ...
  ],
  "totalEndpoints": 3,
  "healthyEndpoints": 3,
  "unhealthyEndpoints": 0
}
```

### Test 2: Get Detailed Metrics
```bash
curl http://localhost:8080/health/detailed
```

### Test 3: View History
```bash
curl "http://localhost:8080/health/history/service-1?limit=10"
```

---

## What's Happening?

1. **Spring Boot starts** on port 8080
2. **Background task** checks 3 services every 30 seconds
3. **Console logs** show health check results
4. **REST API** is ready to serve health data

---

## Troubleshooting

**Problem:** Port 8080 already in use  
**Solution:** Change port in `application.properties`:
```properties
server.port=8081
```

**Problem:** Maven not found  
**Solution:** Install Maven:
```bash
# Ubuntu/Debian
sudo apt install maven

# macOS
brew install maven

# Windows
choco install maven
```

---

## Project Files Explained

- **HealthCheckApplication.java** - Starts Spring Boot
- **HealthCheckController.java** - REST API endpoints
- **HealthCheckService.java** - Health check logic
- **application.properties** - Configuration
- **pom.xml** - Dependencies

---

## Interview Submission

Submit these files:
1. ✅ All Java source files (controller, service, models, config)
2. ✅ pom.xml
3. ✅ application.properties
4. ✅ README.md
5. ✅ ANALYSIS.md (your written analysis)

**ZIP the entire `health-check-springboot` folder and submit.**
