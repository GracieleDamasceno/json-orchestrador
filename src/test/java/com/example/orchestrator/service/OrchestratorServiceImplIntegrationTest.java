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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.retry.support.RetryTemplate;

import com.example.orchestrator.model.StepExecutionResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.example.orchestrator.validation.InputValidator;

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
    @Mock
    private InputValidator inputValidator;
    @Mock
    private RetryTemplate retryTemplate;

    @InjectMocks
    private OrchestratorServiceImpl orchestratorService;

    @BeforeEach
    void setUp() {
        // Manually inject the list of mock executors
        List<ActionExecutor> actionExecutors = Arrays.asList(httpActionExecutor);
        orchestratorService = new OrchestratorServiceImpl(specLoaderService, actionExecutors, inputValidator, retryTemplate);

        // Default behavior for retryTemplate to just execute the callback immediately
        lenient().when(retryTemplate.execute(any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // Create a dummy RetryContext for the mock
                org.springframework.retry.RetryContext retryContext = mock(org.springframework.retry.RetryContext.class);
                when(retryContext.getRetryCount()).thenReturn(0); // Default to 0 retries for successful path
                return invocation.getArgument(0, org.springframework.retry.RetryCallback.class).doWithRetry(retryContext);
            }
        });

        

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
        when(httpActionExecutor.execute(eq(step1), any(ExecutionContext.class), eq(Collections.emptyMap())))
                .thenReturn(Map.of("data", "result from step 1"));
        when(httpActionExecutor.execute(eq(step2), any(ExecutionContext.class), eq(Collections.emptyMap())))
                .thenReturn(Map.of("data", "result from step 2"));

        // Execute orchestration
        Map<String, Object> result = orchestratorService.executeOrchestration(product, requestParams);

        // Verify interactions
        verify(specLoaderService, times(1)).loadSpec(product);
        verify(httpActionExecutor, times(1)).execute(eq(step1), any(ExecutionContext.class), eq(Collections.emptyMap()));
        verify(httpActionExecutor, times(1)).execute(eq(step2), any(ExecutionContext.class), eq(Collections.emptyMap()));

        // Verify the final result
        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertTrue(result.containsKey("context"));
        assertTrue(result.containsKey("trace"));

        List<StepExecutionResult> trace = (List<StepExecutionResult>) result.get("trace");
        assertEquals(2, trace.size());

        assertEquals("step1-id", trace.get(0).stepId());
        assertEquals("success", trace.get(0).status());
        assertNotNull(trace.get(0).output());

        assertEquals("step2-id", trace.get(1).stepId());
        assertEquals("success", trace.get(1).status());
        assertNotNull(trace.get(1).output());

        Map<String, Object> context = (Map<String, Object>) result.get("context");
        assertTrue(context.containsKey("step1Result"));
        assertTrue(context.containsKey("step2Result"));
    }

    @Test
    void executeOrchestration_shouldReturnSuccessWithTrace() throws SpecNotFoundException {
        String product = "testProduct";
        Map<String, Object> requestParams = Collections.emptyMap();

        Step step1 = new Step("step1-id", "http", "GET", "http://example.com/api/step1", Collections.emptyMap(), null, null, null, "step1Result");
        Step step2 = new Step("step2-id", "http", "GET", "http://example.com/api/step2", Collections.emptyMap(), null, null, null, "step2Result");

        Specification specification = new Specification("testProduct", "Test Description", null, Arrays.asList(step1, step2), null);

        when(specLoaderService.loadSpec(product)).thenReturn(specification);
        when(httpActionExecutor.getType()).thenReturn("http");
        when(httpActionExecutor.execute(eq(step1), any(ExecutionContext.class), eq(Collections.emptyMap())))
                .thenReturn(Map.of("data", "result from step 1"));
        when(httpActionExecutor.execute(eq(step2), any(ExecutionContext.class), eq(Collections.emptyMap())))
                .thenReturn(Map.of("data", "result from step 2"));

        Map<String, Object> result = orchestratorService.executeOrchestration(product, requestParams);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertTrue(result.containsKey("context"));
        assertTrue(result.containsKey("trace"));

        List<StepExecutionResult> trace = (List<StepExecutionResult>) result.get("trace");
        assertEquals(2, trace.size());

        assertEquals("step1-id", trace.get(0).stepId());
        assertEquals("success", trace.get(0).status());
        assertNotNull(trace.get(0).output());

        assertEquals("step2-id", trace.get(1).stepId());
        assertEquals("success", trace.get(1).status());
        assertNotNull(trace.get(1).output());

        Map<String, Object> context = (Map<String, Object>) result.get("context");
        assertTrue(context.containsKey("step1Result"));
        assertTrue(context.containsKey("step2Result"));
    }

    @Test
    void executeOrchestration_shouldHaltOnStepFailureAndReturnErrorWithTrace() throws SpecNotFoundException {
        String product = "testProduct";
        Map<String, Object> requestParams = Collections.emptyMap();

        Step step1 = new Step("step1-id", "http", "GET", "http://example.com/api/step1", Collections.emptyMap(), null, null, null, "step1Result");
        Step step2 = new Step("step2-id", "http", "GET", "http://example.com/api/step2", Collections.emptyMap(), null, null, null, "step2Result");
        Step step3 = new Step("step3-id", "http", "GET", "http://example.com/api/step3", Collections.emptyMap(), null, null, null, "step3Result");


        Specification specification = new Specification("testProduct", "Test Description", null, Arrays.asList(step1, step2, step3), null);

        when(specLoaderService.loadSpec(product)).thenReturn(specification);
        when(httpActionExecutor.getType()).thenReturn("http");

        when(httpActionExecutor.execute(eq(step1), any(ExecutionContext.class), eq(Collections.emptyMap())))
                .thenReturn(Map.of("data", "result from step 1"));

        // Simulate step2 failing immediately with the permanent error, as retryTemplate is mocked to not retry in setUp()
        when(httpActionExecutor.execute(eq(step2), any(ExecutionContext.class), eq(Collections.emptyMap())))
                .thenThrow(new RuntimeException("Simulated permanent error for step2"));

        Map<String, Object> result = orchestratorService.executeOrchestration(product, requestParams);

        assertNotNull(result);
        assertEquals("error", result.get("status"));
        assertTrue(result.containsKey("error"));
        assertTrue(result.containsKey("trace"));

        Map<String, Object> errorDetails = (Map<String, Object>) result.get("error");
        assertEquals("Orchestration failed: Simulated permanent error for step2", errorDetails.get("message"));
        assertEquals("step2-id", errorDetails.get("step"));

        List<StepExecutionResult> trace = (List<StepExecutionResult>) result.get("trace");
        assertEquals(2, trace.size()); // Only step1 and step2 should be in trace

        assertEquals("step1-id", trace.get(0).stepId());
        assertEquals("success", trace.get(0).status());

        assertEquals("step2-id", trace.get(1).stepId());
        assertEquals("error", trace.get(1).status());
        assertTrue(trace.get(1).error().contains("Simulated permanent error for step2"));

        verify(specLoaderService, times(1)).loadSpec(product);
        verify(httpActionExecutor, times(1)).execute(eq(step1), any(ExecutionContext.class), eq(Collections.emptyMap()));
        // Verify that step3 was never attempted
        verify(httpActionExecutor, never()).execute(eq(step3), any(ExecutionContext.class), eq(Collections.emptyMap()));
    }

    @Test
    void executeOrchestration_shouldHandleSpecNotFoundException() throws SpecNotFoundException {
        String product = "nonExistentProduct";
        Map<String, Object> requestParams = Collections.emptyMap();

        when(specLoaderService.loadSpec(product)).thenThrow(new SpecNotFoundException("Spec not found"));

        Map<String, Object> result = orchestratorService.executeOrchestration(product, requestParams);

        assertNotNull(result);
        assertEquals("error", result.get("status"));
        Map<String, Object> errorDetails = (Map<String, Object>) result.get("error");
        assertTrue(errorDetails.get("message").toString().contains("Spec not found"));
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
        Map<String, Object> errorDetails = (Map<String, Object>) result.get("error");
        assertTrue(errorDetails.get("message").toString().contains("No action executor found for type: unknownType"));
        verify(specLoaderService, times(1)).loadSpec(product);
        verify(httpActionExecutor, never()).execute(any(), any(), any()); // Executor's execute method should not be called
    }

    @Test
    void executeOrchestration_shouldHandleExecutorExecutionError() throws SpecNotFoundException {
        String product = "testProduct";
        Map<String, Object> requestParams = Collections.emptyMap();

        Step step1 = new Step("step1-id", "http", "GET", "http://example.com/api/step1", Collections.emptyMap(), null, null, null, null);

        Specification specification = new Specification("testProduct", "Test Description", null, Collections.singletonList(step1), null);

        when(specLoaderService.loadSpec(product)).thenReturn(specification);
        when(httpActionExecutor.getType()).thenReturn("http");
        when(httpActionExecutor.execute(eq(step1), any(ExecutionContext.class), eq(Collections.emptyMap())))
                .thenThrow(new IllegalArgumentException("Invalid URL"));

        Map<String, Object> result = orchestratorService.executeOrchestration(product, requestParams);

        assertNotNull(result);
        assertEquals("error", result.get("status"));
        Map<String, Object> errorDetails = (Map<String, Object>) result.get("error");
        assertEquals("Orchestration failed: Invalid URL", errorDetails.get("message").toString());
        assertEquals("step1-id", errorDetails.get("step")); // For executor execution errors, step is the failing step
        verify(specLoaderService, times(1)).loadSpec(product);
        verify(httpActionExecutor, times(1)).execute(eq(step1), any(ExecutionContext.class), eq(Collections.emptyMap()));
    }
}