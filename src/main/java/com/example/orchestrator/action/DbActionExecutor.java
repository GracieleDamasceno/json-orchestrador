package com.example.orchestrator.action;

import com.example.orchestrator.model.GenericEntity;
import com.example.orchestrator.model.Step;
import com.example.orchestrator.repository.GenericEntityRepository;
import com.example.orchestrator.util.ExecutionContext;
import com.example.orchestrator.util.VariableResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DbActionExecutor implements ActionExecutor {

    private final GenericEntityRepository genericEntityRepository;
    private final VariableResolver variableResolver;

    @Autowired
    public DbActionExecutor(GenericEntityRepository genericEntityRepository, VariableResolver variableResolver) {
        this.genericEntityRepository = genericEntityRepository;
        this.variableResolver = variableResolver;
    }

    @Override
    public String getType() {
        return "db";
    }

    @Override
    public Object execute(Step step, ExecutionContext context, Map<String, Object> requestParams) {
        log.info("Executing DB step: {} with operation: {} on table: {}", step.id(), step.operation(), step.table());

        Map<String, Object> resolutionContext = new HashMap<>(requestParams);
        resolutionContext.putAll(context.getMap());

        String tableName = variableResolver.resolveVariables(step.table(), resolutionContext);
        if (tableName == null || tableName.isEmpty()) {
            log.error("Resolved table name cannot be null or empty for DB step: {}", step.id());
            throw new IllegalArgumentException("Resolved table name cannot be null or empty for DB step.");
        }
        log.debug("Resolved table name for step {}: {}", step.id(), tableName);

        switch (step.operation().toLowerCase()) {
            case "select":
                List<GenericEntity> entities = genericEntityRepository.findByTableName(tableName);
                log.info("DB SELECT step '{}' executed successfully. Found {} entities.", step.id(), entities.size());
                return entities;
            case "insert":
                if (step.data() == null || step.data().isEmpty()) {
                    log.error("Data for insert operation cannot be null or empty for DB step: {}", step.id());
                    throw new IllegalArgumentException("Data for insert operation cannot be null or empty.");
                }
                // Convert JsonNode to Map<String, Object> for variable resolution
                Map<String, Object> dataMap = new ObjectMapper().convertValue(step.data(), Map.class);
                Map<String, Object> resolvedData = variableResolver.resolveVariables(dataMap, resolutionContext);
                GenericEntity newEntity = new GenericEntity();
                newEntity.setTableName(tableName);
                newEntity.setData(resolvedData);
                GenericEntity savedEntity = genericEntityRepository.save(newEntity);
                log.info("DB INSERT step '{}' executed successfully. Saved entity with ID: {}", step.id(), savedEntity.getId());
                Map<String, Object> result = new HashMap<>();
                result.put("id", savedEntity.getId());
                result.put("tableName", savedEntity.getTableName());
                result.put("data", savedEntity.getData());
                return result;
            default:
                log.error("Unsupported DB operation for step '{}': {}", step.id(), step.operation());
                throw new UnsupportedOperationException("Operation " + step.operation() + " not supported for db action.");
        }
    }
}