package com.example.orchestrator.controller;

import com.example.orchestrator.service.InvalidInputException;
import com.example.orchestrator.service.OrchestratorService;
import com.example.orchestrator.service.SpecNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrchestratorController {

    private final OrchestratorService orchestratorService;

    public OrchestratorController(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping("/orchestrate")
    public ResponseEntity<?> orchestrate(@RequestBody Map<String, Object> requestBody) {
        String product = (String) requestBody.get("product");
        try {
            Map<String, Object> serviceResult = orchestratorService.executeOrchestration(product, requestBody);
            return ResponseEntity.ok(serviceResult);
        } catch (SpecNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", "error", "message", e.getMessage()));
        } catch (InvalidInputException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}