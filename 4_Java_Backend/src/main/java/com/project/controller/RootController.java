package com.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Industrial Monitor Backend");
        body.put("docs", "/swagger-ui.html");
        body.put("endpoints", Map.of(
                "health", "/api/health",
                "plcData", "/api/plc-data",
                "plcDataAlias", "/api/data",
                "swagger", "/swagger-ui.html",
                "openapi", "/api-docs",
                "actuatorHealth", "/actuator/health"
        ));
        return ResponseEntity.ok(body);
    }
}
