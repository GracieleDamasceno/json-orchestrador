package com.example.orchestrator.action;

import com.example.orchestrator.model.Step;
import com.example.orchestrator.util.ExecutionContext;
import com.example.orchestrator.util.VariableNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
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

        Object result = httpActionExecutor.execute(step, context, Collections.emptyMap());

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
            httpActionExecutor.execute(step, context, Collections.emptyMap());
        });

        assertTrue(thrown.getMessage().contains("Only GET method is supported"));
    }

    @Test
    void execute_shouldThrowExceptionForNonHttpStepType() {
        Step step = new Step(null, "db", "GET", "http://test.com/api/data", null, null, null, null, null);

        ExecutionContext context = new ExecutionContext();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            httpActionExecutor.execute(step, context, Collections.emptyMap());
        });

        assertTrue(thrown.getMessage().contains("HttpActionExecutor can only handle 'http' type steps."));
    }

    @Test
    void execute_shouldThrowExceptionForNullUrl() {
        Step step = new Step(null, "http", "GET", null, null, null, null, null, null);

        ExecutionContext context = new ExecutionContext();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            httpActionExecutor.execute(step, context, Collections.emptyMap());
        });

        assertTrue(thrown.getMessage().contains("URL cannot be null or empty"));
    }

    @Test
    void execute_shouldThrowExceptionForEmptyUrl() {
        Step step = new Step(null, "http", "GET", "", null, null, null, null, null);

        ExecutionContext context = new ExecutionContext();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            httpActionExecutor.execute(step, context, Collections.emptyMap());
        });

        assertTrue(thrown.getMessage().contains("URL cannot be null or empty"));
    }
    @Test
    void execute_shouldResolveUrlVariablesFromRequestParams() {
        String templateUrl = "http://test.com/api/${id}";
        String expectedUrl = "http://test.com/api/123";
        String expectedResponseBody = "{\"status\": \"success\"}";

        Map<String, Object> requestParams = Map.of("id", "123");
        ExecutionContext context = new ExecutionContext();

        mockServer.expect(requestTo(expectedUrl))
                .andRespond(withSuccess(expectedResponseBody, MediaType.APPLICATION_JSON));

        Step step = new Step(null, "http", "GET", templateUrl, null, null, null, null, null);

        Object result = httpActionExecutor.execute(step, context, requestParams);

        assertNotNull(result);
        mockServer.verify();
    }

    @Test
    void execute_shouldResolveUrlVariablesFromExecutionContext() {
        String templateUrl = "http://test.com/api/${id}";
        String expectedUrl = "http://test.com/api/456";
        String expectedResponseBody = "{\"status\": \"success\"}";

        Map<String, Object> requestParams = Collections.emptyMap();
        ExecutionContext context = new ExecutionContext();
        context.put("id", "456");

        mockServer.expect(requestTo(expectedUrl))
                .andRespond(withSuccess(expectedResponseBody, MediaType.APPLICATION_JSON));

        Step step = new Step(null, "http", "GET", templateUrl, null, null, null, null, null);

        Object result = httpActionExecutor.execute(step, context, requestParams);

        assertNotNull(result);
        mockServer.verify();
    }

    @Test
    void execute_shouldResolveUrlVariablesFromCombinedContext() {
        String templateUrl = "http://test.com/api/${param1}/${param2}";
        String expectedUrl = "http://test.com/api/value1/value2";
        String expectedResponseBody = "{\"status\": \"success\"}";

        Map<String, Object> requestParams = Map.of("param1", "value1");
        ExecutionContext context = new ExecutionContext();
        context.put("param2", "value2");

        mockServer.expect(requestTo(expectedUrl))
                .andRespond(withSuccess(expectedResponseBody, MediaType.APPLICATION_JSON));

        Step step = new Step(null, "http", "GET", templateUrl, null, null, null, null, null);

        Object result = httpActionExecutor.execute(step, context, requestParams);

        assertNotNull(result);
        mockServer.verify();
    }

    @Test
    void execute_shouldResolveHeaderVariablesFromRequestParams() {
        String testUrl = "http://test.com/api/data";
        String expectedResponseBody = "{\"status\": \"success\"}";
        Map<String, String> headers = Map.of("Authorization", "Bearer ${token}");

        Map<String, Object> requestParams = Map.of("token", "reqToken");
        ExecutionContext context = new ExecutionContext();

        mockServer.expect(requestTo(testUrl))
                .andExpect(header("Authorization", "Bearer reqToken"))
                .andRespond(withSuccess(expectedResponseBody, MediaType.APPLICATION_JSON));

        Step step = new Step(null, "http", "GET", testUrl, headers, null, null, null, null);

        Object result = httpActionExecutor.execute(step, context, requestParams);

        assertNotNull(result);
        mockServer.verify();
    }

    @Test
    void execute_shouldResolveHeaderVariablesFromExecutionContext() {
        String testUrl = "http://test.com/api/data";
        String expectedResponseBody = "{\"status\": \"success\"}";
        Map<String, String> headers = Map.of("X-Custom-Header", "${headerValue}");

        Map<String, Object> requestParams = Collections.emptyMap();
        ExecutionContext context = new ExecutionContext();
        context.put("headerValue", "contextValue");

        mockServer.expect(requestTo(testUrl))
                .andExpect(header("X-Custom-Header", "contextValue"))
                .andRespond(withSuccess(expectedResponseBody, MediaType.APPLICATION_JSON));

        Step step = new Step(null, "http", "GET", testUrl, headers, null, null, null, null);

        Object result = httpActionExecutor.execute(step, context, requestParams);

        assertNotNull(result);
        mockServer.verify();
    }

    @Test
    void execute_shouldResolveHeaderVariablesFromCombinedContext() {
        String testUrl = "http://test.com/api/data";
        String expectedResponseBody = "{\"status\": \"success\"}";
        Map<String, String> headers = Map.of("Auth", "${authType} ${authToken}");

        Map<String, Object> requestParams = Map.of("authType", "Basic");
        ExecutionContext context = new ExecutionContext();
        context.put("authToken", "contextAuth");

        mockServer.expect(requestTo(testUrl))
                .andExpect(header("Auth", "Basic contextAuth"))
                .andRespond(withSuccess(expectedResponseBody, MediaType.APPLICATION_JSON));

        Step step = new Step(null, "http", "GET", testUrl, headers, null, null, null, null);

        Object result = httpActionExecutor.execute(step, context, requestParams);

        assertNotNull(result);
        mockServer.verify();
    }

    @Test
    void execute_shouldThrowExceptionIfUrlVariableNotFound() {
        String templateUrl = "http://test.com/api/${id}";
        Map<String, Object> requestParams = Collections.emptyMap();
        ExecutionContext context = new ExecutionContext();

        Step step = new Step(null, "http", "GET", templateUrl, null, null, null, null, null);

        VariableNotFoundException thrown = assertThrows(VariableNotFoundException.class, () -> {
            httpActionExecutor.execute(step, context, requestParams);
        });

        assertTrue(thrown.getMessage().contains("Variable 'id' not found in context."));
    }

    @Test
    void execute_shouldThrowExceptionIfHeaderVariableNotFound() {
        String testUrl = "http://test.com/api/data";
        Map<String, String> headers = Map.of("Authorization", "Bearer ${token}");

        Map<String, Object> requestParams = Collections.emptyMap();
        ExecutionContext context = new ExecutionContext();

        Step step = new Step(null, "http", "GET", testUrl, headers, null, null, null, null);

        VariableNotFoundException thrown = assertThrows(VariableNotFoundException.class, () -> {
            httpActionExecutor.execute(step, context, requestParams);
        });

        assertTrue(thrown.getMessage().contains("Variable 'token' not found in context."));
    }
}