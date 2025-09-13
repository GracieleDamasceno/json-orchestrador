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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;

import com.example.orchestrator.validation.InputValidator;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrchestratorServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorServiceTest.class);

    @Mock
    private SpecLoaderService specLoaderService;

    @Mock
    private InputValidator inputValidator;
    @Mock
    private RetryTemplate retryTemplate;
    @Mock
    private OutputFormatter outputFormatter;

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

        // Manually inject the list of mock executors (empty for this test)
        List<com.example.orchestrator.action.ActionExecutor> actionExecutors = Collections.emptyList();
        orchestratorService = new OrchestratorServiceImpl(specLoaderService, actionExecutors, inputValidator, retryTemplate, outputFormatter);

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
    void executeOrchestration_shouldReturnHardcodedResponse() throws SpecNotFoundException, IOException {
        String product = "testProduct";
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("product", product);
        requestParams.put("param1", "value1");

        when(specLoaderService.loadSpec(product)).thenReturn(dummySpecification);

        Map<String, Object> result = orchestratorService.executeOrchestration(product, requestParams);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertNotNull(result.get("output")); // Assert for output instead of context
        assertNotNull(result.get("trace"));
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
        Map<String, Object> errorDetails = (Map<String, Object>) result.get("error");
        assertEquals("Specification for product 'nonExistentProduct' not found.", errorDetails.get("message"));
    }
}