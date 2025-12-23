package com.gideon.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResult {
    private String endpoint;
    private HealthCheckStatus status;
    private long responseTime;
    private Integer statusCode;
    private String error;
    private String timestamp;
}