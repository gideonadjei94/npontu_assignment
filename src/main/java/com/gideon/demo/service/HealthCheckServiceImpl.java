package com.gideon.demo.service;


import com.gideon.demo.config.EndpointConfig;
import com.gideon.demo.dto.HealthCheckResult;
import com.gideon.demo.dto.HealthCheckStatus;
import com.gideon.demo.dto.HealthMetrics;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HealthCheckServiceImpl implements HealthCheckService{

    @Autowired
    private RestTemplate restTemplate;

    private List<EndpointConfig> endpoints;
    private Map<String, List<HealthCheckResult>> healthHistory;
    private long startTime;

    @PostConstruct
    public void init() {
        this.healthHistory = new ConcurrentHashMap<>();
        this.startTime = System.currentTimeMillis();

        // Configure the 3 endpoints to monitor
        this.endpoints = Arrays.asList(
                new EndpointConfig("service-1", "https://knowmate.com", 5000),
                new EndpointConfig("service-2", "https://ppmt.myclassform.com", 5000),
                new EndpointConfig("service-3", "https://myclassform.com", 5000)
        );

        // Initialize history storage for each endpoint
        endpoints.forEach(endpoint ->
                healthHistory.put(endpoint.getName(), Collections.synchronizedList(new ArrayList<>()))
        );

        log.info("Health Check Service initialized - Monitoring {} endpoints", endpoints.size());
    }



    /**
     * Check a single endpoint's health
     */
    @Override
    public HealthCheckResult checkEndpoint(EndpointConfig endpoint) {
        long startTime = System.currentTimeMillis();
        String timestamp = Instant.now().toString();

        try {
            // Make HTTP request to the endpoint
            ResponseEntity<String> response = restTemplate.getForEntity(
                    endpoint.getUrl(),
                    String.class
            );

            long responseTime = System.currentTimeMillis() - startTime;
            HttpStatusCode statusCode = response.getStatusCode();

            // Build result object
            HealthCheckResult result = HealthCheckResult.builder()
                    .endpoint(endpoint.getName())
                    .status(statusCode.is2xxSuccessful() ? HealthCheckStatus.UP : HealthCheckStatus.DOWN)
                    .responseTime(responseTime)
                    .statusCode(statusCode.value())
                    .timestamp(timestamp)
                    .build();

            addToHistory(endpoint.getName(), result);
            return result;

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;

            // Service is DOWN
            HealthCheckResult result = HealthCheckResult.builder()
                    .endpoint(endpoint.getName())
                    .status(HealthCheckStatus.DOWN)
                    .responseTime(responseTime)
                    .error(e.getMessage())
                    .timestamp(timestamp)
                    .build();

            addToHistory(endpoint.getName(), result);
            log.error("Health check failed for {}: {}", endpoint.getName(), e.getMessage());
            return result;
        }
    }



    /**
     * Add result to history (keep last 100 checks per endpoint)
     */
    @Override
    public void addToHistory(String endpointName, HealthCheckResult result) {
        List<HealthCheckResult> history = healthHistory.get(endpointName);
        if (history != null) {
            history.add(result);
            // Keep only last 100 checks to avoid memory issues
            if (history.size() > 100) {
                history.removeFirst();
            }
        }
    }



    /**
     * Perform health checks on all endpoints (in parallel for speed)
     */
    @Override
    public HealthMetrics performHealthChecks() {
        // Execute checks in parallel using CompletableFuture
        List<CompletableFuture<HealthCheckResult>> futures = endpoints.stream()
                .map(endpoint -> CompletableFuture.supplyAsync(() -> checkEndpoint(endpoint)))
                .toList();

        // Wait for all to complete
        List<HealthCheckResult> checks = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // Calculate summary statistics
        long healthy = checks.stream().filter(c -> HealthCheckStatus.UP.equals(c.getStatus())).count();
        long unhealthy = checks.stream().filter(c -> HealthCheckStatus.DOWN.equals(c.getStatus())).count();

        // Determine overall status
        String overallStatus;
        if (unhealthy == 0) {
            overallStatus = "HEALTHY";
        } else if (healthy > unhealthy) {
            overallStatus = "DEGRADED";
        } else {
            overallStatus = "UNHEALTHY";
        }

        return HealthMetrics.builder()
                .overallStatus(overallStatus)
                .checks(checks)
                .totalEndpoints(checks.size())
                .healthyEndpoints((int) healthy)
                .unhealthyEndpoints((int) unhealthy)
                .uptime(System.currentTimeMillis() - this.startTime)
                .lastCheck(Instant.now().toString())
                .build();
    }




    /**
     * Get historical health data for a specific endpoint
     */
    @Override
    public List<HealthCheckResult> getEndpointHistory(String endpointName, int limit) {
        List<HealthCheckResult> history = healthHistory.get(endpointName);
        if (history == null || history.isEmpty()) {
            return Collections.emptyList();
        }

        int size = history.size();
        int fromIndex = Math.max(0, size - limit);
        return new ArrayList<>(history.subList(fromIndex, size));
    }




    /**
     * Calculate availability metrics for an endpoint
     */
    @Override
    public Map<String, Object> getAvailabilityMetrics(String endpointName) {
        List<HealthCheckResult> history = healthHistory.get(endpointName);
        if (history == null || history.isEmpty()) {
            return Map.of(
                    "availability", 0.0,
                    "avgResponseTime", 0.0,
                    "totalChecks", 0
            );
        }

        long upCount = history.stream().filter(h -> HealthCheckStatus.UP.equals(h.getStatus())).count();
        double availability = (upCount * 100.0) / history.size();

        double avgResponseTime = history.stream()
                .mapToLong(HealthCheckResult::getResponseTime)
                .average()
                .orElse(0.0);

        return Map.of(
                "availability", Math.round(availability * 100.0) / 100.0,
                "avgResponseTime", Math.round(avgResponseTime * 100.0) / 100.0,
                "totalChecks", history.size()
        );
    }




    /**
     * Get detailed metrics with availability stats for each endpoint
     */
    @Override
    public Map<String, Object> getDetailedMetrics() {
        HealthMetrics metrics = performHealthChecks();

        List<Map<String, Object>> endpointDetails = endpoints.stream()
                .map(endpoint -> {
                    Map<String, Object> details = new HashMap<>(getAvailabilityMetrics(endpoint.getName()));
                    details.put("name", endpoint.getName());
                    details.put("url", endpoint.getUrl());
                    return details;
                })
                .collect(Collectors.toList());

        Map<String, Object> detailed = new HashMap<>();
        detailed.put("overallStatus", metrics.getOverallStatus());
        detailed.put("checks", metrics.getChecks());
        detailed.put("summary", Map.of(
                "total", metrics.getTotalEndpoints(),
                "healthy", metrics.getHealthyEndpoints(),
                "unhealthy", metrics.getUnhealthyEndpoints()
        ));
        detailed.put("uptime", metrics.getUptime());
        detailed.put("lastCheck", metrics.getLastCheck());
        detailed.put("endpointDetails", endpointDetails);

        return detailed;
    }




    /**
     * Scheduled background health checks (runs every 30 seconds)
     * This continuously monitors services even when no one calls the API
     */
    @Scheduled(fixedRate = 30000)
    public void scheduledHealthCheck() {
        HealthMetrics metrics = performHealthChecks();

        log.info("[{}] Health Check: {} - {}/{} services healthy",
                Instant.now(),
                metrics.getOverallStatus(),
                metrics.getHealthyEndpoints(),
                metrics.getTotalEndpoints()
        );

        // Log alerts for any DOWN services
        metrics.getChecks().stream()
                .filter(check -> HealthCheckStatus.DOWN.equals(check.getStatus()))
                .forEach(check ->
                        log.error("ALERT: {} is DOWN - {}",
                                check.getEndpoint(),
                                check.getError() != null ? check.getError() : "Unknown error")
                );
    }
}
