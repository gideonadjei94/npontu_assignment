package com.gideon.demo.service;

import com.gideon.demo.config.EndpointConfig;
import com.gideon.demo.dto.HealthCheckResult;
import com.gideon.demo.dto.HealthMetrics;

import java.util.List;
import java.util.Map;

public interface HealthCheckService {
    HealthCheckResult checkEndpoint(EndpointConfig endpoint);
    void addToHistory(String endpointName, HealthCheckResult result);
    HealthMetrics performHealthChecks();
    List<HealthCheckResult> getEndpointHistory(String endpointName, int limit);
    Map<String, Object> getAvailabilityMetrics(String endpointName);
    Map<String, Object> getDetailedMetrics();
}
