package com.gideon.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthMetrics {
    private String overallStatus;
    private List<HealthCheckResult> checks;
    private int totalEndpoints;
    private int healthyEndpoints;
    private int unhealthyEndpoints;
    private long uptime;
    private String lastCheck;
}
