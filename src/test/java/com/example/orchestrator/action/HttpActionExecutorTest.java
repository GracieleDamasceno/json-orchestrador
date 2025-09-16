package com.example.orchestrator.action;

import com.example.orchestrator.model.Step;
import com.example.orchestrator.util.ExecutionContext;
import com.example.orchestrator.util.VariableNotFoundException;
import com.example.orchestrator.util.VariableResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpActionExecutorTest {

    @Mock
    private RestClient restClient;
    @Mock
    private VariableResolver variableResolver;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private HttpActionExecutor httpActionExecutor;

    @BeforeEach
    void setUp() {
        // Mockito will inject mocks automatically
    }

    @Test
    void getType_shouldReturnHttp() {
        assertEquals("http", httpActionExecutor.getType());
    }

    @Test
    void execute_shouldPerformHttpGetRequestAndReturnBody() throws Exception {
        String testUrl = "http://test.com/api/data";
        String expectedResponseBody = "{\"id\": 1, \"name\": \"Test Data\"}";
        Map<String, Object> expectedResponseMap = Map.of("id", 1, "name", "Test Data");

        Step step = new Step(null, "http", "GET", testUrl, null, null, null, null, null);

        ExecutionContext context = new ExecutionContext();
        Map<String, Object> requestParams = Collections.emptyMap();

        when(variableResolver.resolveVariables(eq(testUrl), any(Map.class))).thenReturn(testUrl);

        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(testUrl)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(eq(Map.class))).thenReturn(expectedResponseMap);

        Object result = httpActionExecutor.execute(step, context, requestParams);

        assertNotNull(result);
        assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertEquals(1, resultMap.get("id"));
        assertEquals("Test Data", resultMap.get("name"));
    }

    @Test
    void execute_shouldPerformHttpPostRequestAndReturnBody() throws Exception {
        String testUrl = "http://test.com/api/data";
        String requestBodyJson = "{\"key\": \"value\"}";
        String expectedResponseBody = "{\"status\": \"posted\"}";
        Map<String, Object> requestBodyMap = Map.of("key", "value");
        Map<String, Object> expectedResponseMap = Map.of("status", "posted");

        Map<String, String> headers = Map.of("Content-Type", "application/json");
        JsonNode requestData = new ObjectMapper().readTree(requestBodyJson);

        Step step = new Step(null, "http", "POST", testUrl, headers, null, null, requestData, null);

        ExecutionContext context = new ExecutionContext();
        Map<String, Object> requestParams = Collections.emptyMap();

        when(variableResolver.resolveVariables(eq(testUrl), any(Map.class))).thenReturn(testUrl);
        when(objectMapper.convertValue(any(JsonNode.class), eq(Map.class))).thenReturn(requestBodyMap);
        when(variableResolver.resolveVariables(eq(requestBodyMap), any(Map.class))).thenReturn(requestBodyMap);
        when(variableResolver.resolveVariables(eq("application/json"), any(Map.class))).thenReturn("application/json");

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(testUrl)).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(eq(requestBodyMap))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(eq(Map.class))).thenReturn(expectedResponseMap);

        Object result = httpActionExecutor.execute(step, context, requestParams);

        assertNotNull(result);
        assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertEquals("posted", resultMap.get("status"));
    }

    @Test
    void execute_shouldThrowExceptionForUnsupportedMethod() {
        Step step = new Step(null, "http", "PUT", "http://test.com/api/data", null, null, null, null, null);

        ExecutionContext context = new ExecutionContext();
        Map<String, Object> requestParams = Collections.emptyMap();

        when(variableResolver.resolveVariables(eq("http://test.com/api/data"), any(Map.class))).thenReturn("http://test.com/api/data");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            httpActionExecutor.execute(step, context, requestParams);
        });

        assertTrue(thrown.getMessage().contains("Unsupported HTTP method"));
    }

    @Test
    void execute_shouldThrowExceptionForNonHttpStepType() {
        Step step = new Step(null, "db", "GET", "http://test.com/api/data", null, null, null, null, null);

        ExecutionContext context = new ExecutionContext();
        Map<String, Object> requestParams = Collections.emptyMap();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            httpActionExecutor.execute(step, context, requestParams);
        });

        assertTrue(thrown.getMessage().contains("HttpActionExecutor can only handle 'http' type steps."));
    }

    @Test
    void execute_shouldThrowExceptionForNullUrl() {
        Step step = new Step(null, "http", "GET", null, null, null, null, null, null);

        ExecutionContext context = new ExecutionContext();
        Map<String, Object> requestParams = Collections.emptyMap();

        when(variableResolver.resolveVariables(eq(null), any(Map.class))).thenReturn(null);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            httpActionExecutor.execute(step, context, requestParams);
        });

        assertTrue(thrown.getMessage().contains("URL cannot be null or empty"));
    }

    @Test
    void execute_shouldThrowExceptionForEmptyUrl() {
        Step step = new Step(null, "http", "GET", "", null, null, null, null, null);

        ExecutionContext context = new ExecutionContext();
        Map<String, Object> requestParams = Collections.emptyMap();

        when(variableResolver.resolveVariables(eq(""), any(Map.class))).thenReturn("");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            httpActionExecutor.execute(step, context, requestParams);
        });

        assertTrue(thrown.getMessage().contains("URL cannot be null or empty"));
    }

    @Test
    void execute_shouldResolveUrlVariablesFromRequestParams() throws Exception {
        String templateUrl = "http://test.com/api/${id}";
        String expectedUrl = "http://test.com/api/123";
        String expectedResponseBody = "{\"status\": \"success\"}";
        Map<String, Object> expectedResponseMap = Map.of("status", "success");

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("id", "123");
        ExecutionContext context = new ExecutionContext();

        when(variableResolver.resolveVariables(eq(templateUrl), any(Map.class))).thenReturn(expectedUrl);

        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(expectedUrl)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(eq(Map.class))).thenReturn(expectedResponseMap);

        Step step = new Step(null, "http", "GET", templateUrl, null, null, null, null, null);

        Object result = httpActionExecutor.execute(step, context, requestParams);

        assertNotNull(result);
    }

    @Test
    void execute_shouldResolveUrlVariablesFromExecutionContext() throws Exception {
        String templateUrl = "http://test.com/api/${id}";
        String expectedUrl = "http://test.com/api/456";
        String expectedResponseBody = "{\"status\": \"success\"}";
        Map<String, Object> expectedResponseMap = Map.of("status", "success");

        Map<String, Object> requestParams = Collections.emptyMap();
        ExecutionContext context = new ExecutionContext();
        context.put("id", "456");

        when(variableResolver.resolveVariables(eq(templateUrl), any(Map.class))).thenReturn(expectedUrl);

        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(expectedUrl)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(eq(Map.class))).thenReturn(expectedResponseMap);

        Step step = new Step(null, "http", "GET", templateUrl, null, null, null, null, null);

        Object result = httpActionExecutor.execute(step, context, requestParams);

        assertNotNull(result);
    }

    @Test
    void execute_shouldResolveUrlVariablesFromCombinedContext() throws Exception {
        String templateUrl = "http://test.com/api/${param1}/${param2}";
        String expectedUrl = "http://test.com/api/value1/value2";
        String expectedResponseBody = "{\"status\": \"success\"}";
        Map<String, Object> expectedResponseMap = Map.of("status", "success");

        Map<String, Object> requestParams = Map.of("param1", "value1");
        ExecutionContext context = new ExecutionContext();
        context.put("param2", "value2");

        when(variableResolver.resolveVariables(eq(templateUrl), any(Map.class))).thenReturn(expectedUrl);

        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(expectedUrl)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(eq(Map.class))).thenReturn(expectedResponseMap);

        Step step = new Step(null, "http", "GET", templateUrl, null, null, null, null, null);

        Object result = httpActionExecutor.execute(step, context, requestParams);

        assertNotNull(result);
    }

    @Test
    void execute_shouldResolveHeaderVariablesFromRequestParams() throws Exception {
        String testUrl = "http://test.com/api/data";
        String expectedResponseBody = "{\"status\": \"success\"}";
        Map<String, Object> expectedResponseMap = Map.of("status", "success");
        Map<String, String> headers = Map.of("Authorization", "Bearer ${token}");
        String resolvedHeaderValue = "Bearer reqToken";

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("token", "reqToken");
        ExecutionContext context = new ExecutionContext();

        when(variableResolver.resolveVariables(eq(testUrl), any(Map.class))).thenReturn(testUrl);
        when(variableResolver.resolveVariables(eq("Bearer ${token}"), any(Map.class))).thenReturn(resolvedHeaderValue);

        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(testUrl)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(eq(Map.class))).thenReturn(expectedResponseMap);

        Step step = new Step(null, "http", "GET", testUrl, headers, null, null, null, null);

        Object result = httpActionExecutor.execute(step, context, requestParams);

        assertNotNull(result);
    }

    @Test
    void execute_shouldResolveHeaderVariablesFromExecutionContext() throws Exception {
        String testUrl = "http://test.com/api/data";
        String expectedResponseBody = "{\"status\": \"success\"}";
        Map<String, Object> expectedResponseMap = Map.of("status", "success");
        Map<String, String> headers = Map.of("X-Custom-Header", "${headerValue}");
        String resolvedHeaderValue = "contextValue";

        Map<String, Object> requestParams = Collections.emptyMap();
        ExecutionContext context = new ExecutionContext();
        context.put("headerValue", "contextValue");

        when(variableResolver.resolveVariables(eq(testUrl), any(Map.class))).thenReturn(testUrl);
        when(variableResolver.resolveVariables(eq("${headerValue}"), any(Map.class))).thenReturn(resolvedHeaderValue);

        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(testUrl)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(eq(Map.class))).thenReturn(expectedResponseMap);

        Step step = new Step(null, "http", "GET", testUrl, headers, null, null, null, null);

        Object result = httpActionExecutor.execute(step, context, requestParams);

        assertNotNull(result);
    }

    @Test
    void execute_shouldResolveHeaderVariablesFromCombinedContext() throws Exception {
        String testUrl = "http://test.com/api/data";
        String expectedResponseBody = "{\"status\": \"success\"}";
        Map<String, Object> expectedResponseMap = Map.of("status", "success");
        Map<String, String> headers = Map.of("Auth", "${authType} ${authToken}");
        String resolvedHeaderValue = "Basic contextAuth";

        Map<String, Object> requestParams = Map.of("authType", "Basic");
        ExecutionContext context = new ExecutionContext();
        context.put("authToken", "contextAuth");

        when(variableResolver.resolveVariables(eq(testUrl), any(Map.class))).thenReturn(testUrl);
        when(variableResolver.resolveVariables(eq("${authType} ${authToken}"), any(Map.class))).thenReturn(resolvedHeaderValue);

        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(testUrl)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(eq(Map.class))).thenReturn(expectedResponseMap);

        Step step = new Step(null, "http", "GET", testUrl, headers, null, null, null, null);

        Object result = httpActionExecutor.execute(step, context, requestParams);

        assertNotNull(result);
    }

    @Test
    void execute_shouldThrowExceptionIfUrlVariableNotFound() {
        String templateUrl = "http://test.com/api/${id}";
        Map<String, Object> requestParams = Collections.emptyMap();
        ExecutionContext context = new ExecutionContext();

        when(variableResolver.resolveVariables(eq(templateUrl), any(Map.class)))
                .thenThrow(new VariableNotFoundException("Variable 'id' not found in context."));

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

        when(variableResolver.resolveVariables(eq(testUrl), any(Map.class))).thenReturn(testUrl);
        when(variableResolver.resolveVariables(eq("Bearer ${token}"), any(Map.class)))
                .thenThrow(new VariableNotFoundException("Variable 'token' not found in context."));

        Step step = new Step(null, "http", "GET", testUrl, headers, null, null, null, null);

        VariableNotFoundException thrown = assertThrows(VariableNotFoundException.class, () -> {
            httpActionExecutor.execute(step, context, requestParams);
        });

        assertTrue(thrown.getMessage().contains("Variable 'token' not found in context."));
    }
}