package com.example.orchestrator.controller;

import com.example.orchestrator.service.OrchestratorService;
import com.example.orchestrator.service.SpecNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrchestratorController.class)
public class OrchestratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrchestratorService orchestratorService;

    @Test
    void orchestrate_shouldReturnSuccessResponse() throws Exception {
        String product = "testProduct";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("product", product);
        requestBody.put("param1", "value1");

        Map<String, Object> serviceResponse = Map.of("status", "success", "message", "Orchestration for product " + product + " started");

        when(orchestratorService.executeOrchestration(eq(product), any(Map.class)))
                .thenReturn(serviceResponse);

        mockMvc.perform(post("/api/orchestrate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"product\":\"testProduct\", \"param1\":\"value1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Orchestration for product testProduct started"));
    }

    @Test
    void orchestrate_shouldReturnNotFoundForMissingSpec() throws Exception {
        String product = "nonExistentProduct";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("product", product);
        requestBody.put("param1", "value1");

        when(orchestratorService.executeOrchestration(eq(product), any(Map.class)))
                .thenThrow(new SpecNotFoundException("Specification for product '" + product + "' not found."));

        mockMvc.perform(post("/api/orchestrate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"product\":\"nonExistentProduct\", \"param1\":\"value1\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Specification for product 'nonExistentProduct' not found."));
    }
}