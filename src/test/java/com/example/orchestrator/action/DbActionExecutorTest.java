package com.example.orchestrator.action;

import com.example.orchestrator.model.GenericEntity;
import com.example.orchestrator.model.Step;
import com.example.orchestrator.repository.GenericEntityRepository;
import com.example.orchestrator.util.ExecutionContext;
import com.example.orchestrator.util.VariableResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import({DbActionExecutor.class}) // Removed VariableResolver from @Import as it's now mocked statically
class DbActionExecutorTest {

    @Autowired
    private DbActionExecutor dbActionExecutor;

    @Autowired
    private GenericEntityRepository genericEntityRepository;

    // No longer need @MockBean for VariableResolver as it's static
    // @MockBean
    // private VariableResolver mockVariableResolver;

    private static MockedStatic<VariableResolver> mockedVariableResolver;

    @BeforeEach
    void setUp() {
        // Mock the static VariableResolver
        mockedVariableResolver = Mockito.mockStatic(VariableResolver.class);
        mockedVariableResolver.when(() -> VariableResolver.resolveVariables(anyString(), any(Map.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // Return the original string
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        mockedVariableResolver.close();
    }

    @Test
    void getType_shouldReturnDb() {
        assertEquals("db", dbActionExecutor.getType());
    }

    @Test
    void execute_selectOperation_shouldReturnEntities() {
        // Given
        String tableName = "test_table";
        Map<String, String> data = new HashMap<>();
        data.put("key1", "value1");
        GenericEntity entity = new GenericEntity(tableName, data);
        genericEntityRepository.save(entity);

        Step step = new Step("step1", "db", null, null, null, "select", tableName, null, null);
        ExecutionContext context = new ExecutionContext();
        Map<String, Object> requestParams = new HashMap<>();

        // When
        List<GenericEntity> result = (List<GenericEntity>) dbActionExecutor.execute(step, context, requestParams);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(tableName, result.get(0).getTableName());
        assertEquals(data, result.get(0).getData());
    }

    @Test
    void execute_unsupportedOperation_shouldThrowException() {
        // Given
        Step step = new Step("step1", "db", null, null, null, "insert", "test_table", null, null);
        ExecutionContext context = new ExecutionContext();
        Map<String, Object> requestParams = new HashMap<>();

        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> dbActionExecutor.execute(step, context, requestParams));
    }

    @Test
    void execute_selectOperationWithVariable_shouldReturnEntities() {
        // Given
        String tableName = "variable_table";
        Map<String, String> data = new HashMap<>();
        data.put("keyA", "valueA");
        GenericEntity entity = new GenericEntity(tableName, data);
        genericEntityRepository.save(entity);

        String variableTableName = "${input.dynamicTable}";
        mockedVariableResolver.when(() -> VariableResolver.resolveVariables(eq(variableTableName), any(Map.class)))
                .thenReturn(tableName);

        Step step = new Step("step1", "db", null, null, null, "select", variableTableName, null, null);
        ExecutionContext context = new ExecutionContext();
        context.put("input.dynamicTable", tableName); // Simulate input context
        Map<String, Object> requestParams = new HashMap<>();

        // When
        List<GenericEntity> result = (List<GenericEntity>) dbActionExecutor.execute(step, context, requestParams);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(tableName, result.get(0).getTableName());
        assertEquals(data, result.get(0).getData());
    }
}