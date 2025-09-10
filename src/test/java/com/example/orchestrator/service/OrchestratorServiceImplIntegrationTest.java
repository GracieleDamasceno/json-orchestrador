package com.example.orchestrator.service;

import com.example.orchestrator.action.ActionExecutor;
import com.example.orchestrator.model.Output;
import com.example.orchestrator.model.Specification;
import com.example.orchestrator.model.Step;
import com.example.orchestrator.util.ExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrchestratorServiceImplIntegrationTest {

    @Mock
    private SpecLoaderService specLoaderService;

    @Mock
    private ActionExecutor httpActionExecutor; // Mock a specific executor

    @InjectMocks
    private OrchestratorServiceImpl orchestratorService;

    @BeforeEach
    void setUp() {
        // Manually inject the list of mock executors
        List<ActionExecutor> actionExecutors = Arrays.asList(httpActionExecutor);
        orchestratorService = new OrchestratorServiceImpl(specLoaderService, actionExecutors);
    }

    @Test
    void executeOrchestration_shouldExecuteStepsAndStoreOutputs() throws SpecNotFoundException {
        String product = "testProduct";
        Map<String, Object> requestParams = Collections.emptyMap();

        // Mock Specification
        Output output1 = new Output(Collections.emptyList()); // Assuming OutputParameter is not relevant for this test
        Step step1 = new Step("step1-id", "http", "GET", "http://example.com/api/step1", Collections.emptyMap(), null, null, null, "step1Result");

        Output output2 = new Output(Collections.emptyList()); // Assuming OutputParameter is not relevant for this test
        Step step2 = new Step("step2-id", "http", "GET", "http://example.com/api/step2", Collections.emptyMap(), null, null, null, "step2Result");

        Specification specification = new Specification("testProduct", "Test Description", null, Arrays.asList(step1, step2), null);

        when(specLoaderService.loadSpec(product)).thenReturn(specification);

        // Mock HttpActionExecutor behavior
        when(httpActionExecutor.getType()).thenReturn("http");
        when(httpActionExecutor.execute(eq(step1), any(ExecutionContext.class)))
                .thenReturn(Map.of("data", "result from step 1"));
        when(httpActionExecutor.execute(eq(step2), any(ExecutionContext.class)))
                .thenReturn(Map.of("data", "result from step 2"));

        // Execute orchestration
        Map<String, Object> result = orchestratorService.executeOrchestration(product, requestParams);

        // Verify interactions
        verify(specLoaderService, times(1)).loadSpec(product);
        verify(httpActionExecutor, times(1)).execute(eq(step1), any(ExecutionContext.class));
        verify(httpActionExecutor, times(1)).execute(eq(step2), any(ExecutionContext.class));

        // Verify the final result
        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertEquals("Orchestration for product testProduct completed", result.get("message"));

        // Although we can't directly inspect the ExecutionContext from here,
        // the successful execution of steps and the final message imply correct flow.
        // More detailed context verification would require a spy or custom argument captor if needed.
    }

    @Test
    void executeOrchestration_shouldHandleSpecNotFoundException() throws SpecNotFoundException {
        String product = "nonExistentProduct";
        Map<String, Object> requestParams = Collections.emptyMap();

        when(specLoaderService.loadSpec(product)).thenThrow(new SpecNotFoundException("Spec not found"));

        Map<String, Object> result = orchestratorService.executeOrchestration(product, requestParams);

        assertNotNull(result);
        assertEquals("error", result.get("status"));
        assertTrue(result.get("message").toString().contains("Spec not found"));
        verify(specLoaderService, times(1)).loadSpec(product);
        verifyNoInteractions(httpActionExecutor); // No executors should be called
    }

    @Test
    void executeOrchestration_shouldHandleExecutorNotFound() throws SpecNotFoundException {
        String product = "testProduct";
        Map<String, Object> requestParams = Collections.emptyMap();

        Step step1 = new Step("step1-id", "unknownType", "GET", "http://example.com/api/step1", Collections.emptyMap(), null, null, null, null);

        Specification specification = new Specification("testProduct", "Test Description", null, Collections.singletonList(step1), null);

        when(specLoaderService.loadSpec(product)).thenReturn(specification);
        when(httpActionExecutor.getType()).thenReturn("http"); // Only http executor is mocked

        Map<String, Object> result = orchestratorService.executeOrchestration(product, requestParams);

        assertNotNull(result);
        assertEquals("error", result.get("status"));
        assertTrue(result.get("message").toString().contains("No action executor found for type: unknownType"));
        verify(specLoaderService, times(1)).loadSpec(product);
        verify(httpActionExecutor, never()).execute(any(), any()); // Executor's execute method should not be called
    }

    @Test
    void executeOrchestration_shouldHandleExecutorExecutionError() throws SpecNotFoundException {
        String product = "testProduct";
        Map<String, Object> requestParams = Collections.emptyMap();

        Step step1 = new Step("step1-id", "http", "GET", "http://example.com/api/step1", Collections.emptyMap(), null, null, null, null);

        Specification specification = new Specification("testProduct", "Test Description", null, Collections.singletonList(step1), null);

        when(specLoaderService.loadSpec(product)).thenReturn(specification);
        when(httpActionExecutor.getType()).thenReturn("http");
        when(httpActionExecutor.execute(eq(step1), any(ExecutionContext.class)))
                .thenThrow(new IllegalArgumentException("Invalid URL"));

        Map<String, Object> result = orchestratorService.executeOrchestration(product, requestParams);

        assertNotNull(result);
        assertEquals("error", result.get("status"));
        assertTrue(result.get("message").toString().contains("Orchestration failed: Invalid URL"));
        verify(specLoaderService, times(1)).loadSpec(product);
        verify(httpActionExecutor, times(1)).execute(eq(step1), any(ExecutionContext.class));
    }
}