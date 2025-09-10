package com.example.orchestrator.service;

import com.example.orchestrator.model.Input;
import com.example.orchestrator.model.Output;
import com.example.orchestrator.model.Specification;
import com.example.orchestrator.model.Step;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrchestratorServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorServiceTest.class);

    @Mock
    private SpecLoaderService specLoaderService;

    @InjectMocks
    private OrchestratorServiceImpl orchestratorService;

    private Specification dummySpecification;

    @BeforeEach
    void setUp() {
        // Initialize dummy Specification
        Input dummyInput = new Input(Collections.emptyList());
        Output dummyOutput = new Output(Collections.emptyList());
        List<Step> dummySteps = Collections.emptyList();
        dummySpecification = new Specification("testProduct", "A test product specification", dummyInput, dummySteps, dummyOutput);
    }

    @Test
    void executeOrchestration_shouldReturnHardcodedResponse() throws SpecNotFoundException, IOException {
        String product = "testProduct";
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("product", product);
        requestParams.put("param1", "value1");

        when(specLoaderService.loadSpec(product)).thenReturn(dummySpecification);

        Map<String, Object> result = orchestratorService.executeOrchestration(product, requestParams);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertEquals("Orchestration for product " + product + " completed", result.get("message"));
        logger.info("Service test passed for product: {}", product);
    }

    @Test
    void executeOrchestration_specNotFound() throws SpecNotFoundException, IOException {
        String product = "nonExistentProduct";
        Map<String, Object> requestParams = new HashMap<>();

        when(specLoaderService.loadSpec(product)).thenThrow(new SpecNotFoundException("Specification for product '" + product + "' not found."));

        Map<String, Object> result = orchestratorService.executeOrchestration(product, requestParams);

        assertNotNull(result);
        assertEquals("error", result.get("status"));
        assertEquals("Specification for product 'nonExistentProduct' not found.", result.get("message"));
    }
}