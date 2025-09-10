package com.example.orchestrator.action;

import com.example.orchestrator.model.Step;
import com.example.orchestrator.util.ExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpActionExecutorTest {

    private HttpActionExecutor httpActionExecutor;
    private MockRestServiceServer mockServer;
    private RestClient restClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        restClient = restClientBuilder.build();
        httpActionExecutor = new HttpActionExecutor(restClient);
    }

    @Test
    void getType_shouldReturnHttp() {
        assertEquals("http", httpActionExecutor.getType());
    }

    @Test
    void execute_shouldPerformHttpGetRequestAndReturnBody() {
        String testUrl = "http://test.com/api/data";
        String expectedResponseBody = "{\"id\": 1, \"name\": \"Test Data\"}";

        mockServer.expect(requestTo(testUrl))
                .andRespond(withSuccess(expectedResponseBody, MediaType.APPLICATION_JSON));

        Step step = new Step(null, "http", "GET", testUrl, null, null, null, null, null);

        ExecutionContext context = new ExecutionContext();

        Object result = httpActionExecutor.execute(step, context);

        assertNotNull(result);
        assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertEquals(1, resultMap.get("id"));
        assertEquals("Test Data", resultMap.get("name"));

        mockServer.verify();
    }

    @Test
    void execute_shouldThrowExceptionForNonHttpGetMethod() {
        Step step = new Step(null, "http", "POST", "http://test.com/api/data", null, null, null, null, null);

        ExecutionContext context = new ExecutionContext();

        UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class, () -> {
            httpActionExecutor.execute(step, context);
        });

        assertTrue(thrown.getMessage().contains("Only GET method is supported"));
    }

    @Test
    void execute_shouldThrowExceptionForNonHttpStepType() {
        Step step = new Step(null, "db", "GET", "http://test.com/api/data", null, null, null, null, null);

        ExecutionContext context = new ExecutionContext();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            httpActionExecutor.execute(step, context);
        });

        assertTrue(thrown.getMessage().contains("HttpActionExecutor can only handle 'http' type steps."));
    }

    @Test
    void execute_shouldThrowExceptionForNullUrl() {
        Step step = new Step(null, "http", "GET", null, null, null, null, null, null);

        ExecutionContext context = new ExecutionContext();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            httpActionExecutor.execute(step, context);
        });

        assertTrue(thrown.getMessage().contains("URL cannot be null or empty"));
    }

    @Test
    void execute_shouldThrowExceptionForEmptyUrl() {
        Step step = new Step(null, "http", "GET", "", null, null, null, null, null);

        ExecutionContext context = new ExecutionContext();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            httpActionExecutor.execute(step, context);
        });

        assertTrue(thrown.getMessage().contains("URL cannot be null or empty"));
    }
}