package com.gideon.demo.controller;


import com.gideon.demo.dto.HealthCheckResult;
import com.gideon.demo.dto.HealthMetrics;
import com.gideon.demo.service.HealthCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class HealthCheckController {


    @Autowired
    private HealthCheckService healthCheckService;

    /**
     * Main health check endpoint
     * GET /health
     *
     * Returns: 200 (HEALTHY), 207 (DEGRADED), or 503 (UNHEALTHY)
     */
    @GetMapping("/health")
    public ResponseEntity<HealthMetrics> getHealth() {
        HealthMetrics metrics = healthCheckService.performHealthChecks();

        // Return appropriate HTTP status based on overall health
        HttpStatus status;
        switch (metrics.getOverallStatus()) {
            case "HEALTHY":
                status = HttpStatus.OK; // 200
                break;
            case "DEGRADED":
                status = HttpStatus.MULTI_STATUS; // 207
                break;
            default:
                status = HttpStatus.SERVICE_UNAVAILABLE; // 503
                break;
        }

        return new ResponseEntity<>(metrics, status);
    }



    /**
     * Detailed health metrics with availability percentages
     * GET /health/detailed
     */
    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> getDetailedHealth() {
        Map<String, Object> detailed = healthCheckService.getDetailedMetrics();
        return ResponseEntity.ok(detailed);
    }



    /**
     * Get historical health data for a specific endpoint
     * GET /health/history/{endpoint}?limit=10
     */
    @GetMapping("/health/history/{endpoint}")
    public ResponseEntity<?> getEndpointHistory(
            @PathVariable String endpoint,
            @RequestParam(defaultValue = "10") int limit) {

        List<HealthCheckResult> history = healthCheckService.getEndpointHistory(endpoint, limit);

        if (history.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No history found for endpoint: " + endpoint);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("endpoint", endpoint);
        response.put("history", history);
        response.put("metrics", healthCheckService.getAvailabilityMetrics(endpoint));

        return ResponseEntity.ok(response);
    }




    /**
     * Root endpoint - service information
     * GET /
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "Health Check Service");
        info.put("version", "1.0.0");
        info.put("description", "Monitors service health and exposes metrics");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /health", "Get current health status of all services");
        endpoints.put("GET /health/detailed", "Get detailed health metrics with availability");
        endpoints.put("GET /health/history/{endpoint}", "Get historical data for specific endpoint");

        info.put("endpoints", endpoints);

        return ResponseEntity.ok(info);
    }
}
