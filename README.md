# Health Check Service - Spring Boot

## 📋 Project Overview
A REST API service that monitors 3 external endpoints and exposes their health status via `/health` endpoint.

---

## 🏗️ Project Structure
```
health-check-springboot/
├── src/main/java/com/npontu/healthcheck/
│   ├── HealthCheckApplication.java          # Main entry point
│   ├── controller/
│   │   └── HealthCheckController.java       # REST endpoints
│   ├── service/
        └──HealthCheckServiceImpl.java
│   │   └── HealthCheckService.java          # Health check logic
│   ├── dto/
│   │   ├── HealthCheckResult.java           # Individual check result
│   │   ├── HealthMetrics.java               # Overall health metrics
│   │   └── EndpointConfig.java              # Endpoint configuration
│   └── config/
│       └── AppConfig.java                   # Spring beans configuration
├── src/main/resources/
│   └── application.properties               # App configuration
└── pom.xml                                  # Maven dependencies
```

---

## 🚀 How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Steps

1. **Navigate to project directory**
```bash
cd health-check-springboot
```

2. **Build the project**
```bash
./mvnw clean install
```

3. **Run the application**
```bash
./mvnw spring-boot:run
```

The service will start on **http://localhost:8080**

You should see:
```
✅ Health Check Service initialized - Monitoring 3 endpoints
🏥 Health Check: HEALTHY - 3/3 services healthy
```

---

## 📡 API Endpoints

### 1. **GET /health**
Main health check endpoint - returns current status of all monitored services.

**Request:**
```bash
curl http://localhost:8080/health
```

**Response (200 OK - All Healthy):**
```json
{
  "overallStatus": "HEALTHY",
  "checks": [
    {
      "endpoint": "service-1",
      "status": "UP",
      "responseTime": 145,
      "statusCode": 200,
      "timestamp": "2024-12-22T10:30:00.000Z"
    },
    {
      "endpoint": "service-2",
      "status": "UP",
      "responseTime": 230,
      "statusCode": 200,
      "timestamp": "2024-12-22T10:30:01.000Z"
    },
    {
      "endpoint": "service-3",
      "status": "UP",
      "responseTime": 180,
      "statusCode": 200,
      "timestamp": "2024-12-22T10:30:02.000Z"
    }
  ],
  "totalEndpoints": 3,
  "healthyEndpoints": 3,
  "unhealthyEndpoints": 0,
  "uptime": 3600000,
  "lastCheck": "2024-12-22T10:30:02.000Z"
}
```

**Response Codes:**
- `200 OK` - All services healthy
- `207 Multi-Status` - Some services down (degraded)
- `503 Service Unavailable` - Majority services down (unhealthy)

---

### 2. **GET /health/detailed**
Detailed metrics with availability percentages.

**Request:**
```bash
curl http://localhost:8080/health/detailed
```

**Response:**
```json
{
  "overallStatus": "HEALTHY",
  "checks": [...],
  "summary": {
    "total": 3,
    "healthy": 3,
    "unhealthy": 0
  },
  "endpointDetails": [
    {
      "name": "service-1",
      "url": "https://jsonplaceholder.typicode.com/posts/1",
      "availability": 98.5,
      "avgResponseTime": 150.25,
      "totalChecks": 100
    }
  ]
}
```

---

### 3. **GET /health/history/{endpoint}**
Historical health data for a specific endpoint.

**Request:**
```bash
curl "http://localhost:8080/health/history/service-1?limit=5"
```

**Response:**
```json
{
  "endpoint": "service-1",
  "history": [
    {
      "endpoint": "service-1",
      "status": "UP",
      "responseTime": 145,
      "statusCode": 200,
      "timestamp": "2024-12-22T10:29:00.000Z"
    }
  ],
  "metrics": {
    "availability": 98.5,
    "avgResponseTime": 150.25,
    "totalChecks": 100
  }
}
```

---

## ⚙️ Monitored Services

The service monitors these 3 endpoints:

1. **service-1**: https://jsonplaceholder.typicode.com/posts/1
2. **service-2**: https://api.github.com/users/github
3. **service-3**: https://httpbin.org/status/200

To change endpoints, edit `HealthCheckService.java`:
```java
this.endpoints = Arrays.asList(
    new EndpointConfig("your-service", "https://your-url.com", 5000)
);
```

---

## 🔄 Background Monitoring

The service automatically checks all endpoints **every 30 seconds** and logs the results:

```
2024-12-22 10:30:00 🏥 Health Check: HEALTHY - 3/3 services healthy
2024-12-22 10:30:30 🏥 Health Check: DEGRADED - 2/3 services healthy
2024-12-22 10:30:30 ⚠️  ALERT: service-2 is DOWN - Connection timeout
```

---

## 📊 Key Features

✅ **Parallel Health Checks** - Uses CompletableFuture for concurrent checks  
✅ **Historical Tracking** - Stores last 100 checks per endpoint  
✅ **Availability Metrics** - Calculates uptime percentage  
✅ **Response Time Tracking** - Monitors performance degradation  
✅ **Automatic Alerting** - Logs alerts when services go down  
✅ **RESTful API** - Clean JSON responses

---

## 🧪 Testing

```bash
# Test health endpoint
curl http://localhost:8080/health

# Test detailed metrics
curl http://localhost:8080/health/detailed

# Test history for service-1 (limit to 20 results)
curl "http://localhost:8080/health/history/service-1?limit=20"

# Test root endpoint
curl http://localhost:8080/
```

---

## 📝 Code Explanation

### Controller Layer
- Handles HTTP requests
- Returns appropriate HTTP status codes
- Maps endpoints to service methods

### Service Layer
- Contains health check logic
- Performs parallel endpoint checks
- Manages health history
- Calculates metrics
- Scheduled background checks

### Model Layer
- `HealthCheckResult` - Individual check result
- `HealthMetrics` - Overall system health
- `EndpointConfig` - Endpoint configuration

### Configuration
- `AppConfig` - Defines RestTemplate bean
- `application.properties` - Server and logging config

---