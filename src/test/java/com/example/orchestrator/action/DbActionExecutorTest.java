package com.example.orchestrator.action;

import com.example.orchestrator.JsonOrchestratorApplication;
import com.example.orchestrator.model.GenericEntity;
import com.example.orchestrator.model.Step;
import com.example.orchestrator.repository.GenericEntityRepository;
import com.example.orchestrator.util.ExecutionContext;
import com.example.orchestrator.util.VariableResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import({DbActionExecutor.class, VariableResolver.class})
@ActiveProfiles("test")
class DbActionExecutorTest {

    @Autowired
    private DbActionExecutor dbActionExecutor;

    @Autowired
    private GenericEntityRepository genericEntityRepository;

    @Autowired
    private VariableResolver variableResolver;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        genericEntityRepository.deleteAll(); // Clear H2 database before each test
    }

    @Test
    void getType_shouldReturnDb() {
        assertEquals("db", dbActionExecutor.getType());
    }

    @Test
    void execute_selectOperation_shouldReturnEntities() {
        // Given
        String tableName = "test_table";
        Map<String, Object> data = new HashMap<>();
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
    void execute_insertOperation_shouldSaveEntity() throws Exception {
        // Given
        String tableName = "new_table";
        Map<String, Object> insertData = new HashMap<>();
        insertData.put("name", "test_name");
        insertData.put("value", 123);

        Step step = new Step("step1", "db", null, null, null, "insert", tableName, objectMapper.valueToTree(insertData), null);
        ExecutionContext context = new ExecutionContext();
        Map<String, Object> requestParams = new HashMap<>();


        // When
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) dbActionExecutor.execute(step, context, requestParams);

        // Then
        assertNotNull(result);
        assertNotNull(result.get("id"));
        assertEquals(tableName, result.get("tableName"));
        assertEquals(insertData, result.get("data"));

        // Verify it was saved in the repository
        List<GenericEntity> entities = genericEntityRepository.findByTableName(tableName);
        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());
        assertEquals(result.get("id"), entities.get(0).getId());
    }

    @Test
    void execute_unsupportedOperation_shouldThrowException() {
        // Given
        Step step = new Step("step1", "db", null, null, null, "unsupported", "test_table", null, null);
        ExecutionContext context = new ExecutionContext();
        Map<String, Object> requestParams = new HashMap<>();


        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> dbActionExecutor.execute(step, context, requestParams));
    }

    @Test
    void execute_selectOperationWithVariable_shouldReturnEntities() {
        // Given
        String tableName = "variable_table";
        Map<String, Object> data = new HashMap<>();
        data.put("keyA", "valueA");
        GenericEntity entity = new GenericEntity(tableName, data);
        genericEntityRepository.save(entity);

        String variableTableName = "${input.dynamicTable}";

        Step step = new Step("step1", "db", null, null, null, "select", variableTableName, null, null);
        ExecutionContext context = new ExecutionContext();
        Map<String, Object> input = new HashMap<>();
        input.put("dynamicTable", tableName);
        context.put("input", input);
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