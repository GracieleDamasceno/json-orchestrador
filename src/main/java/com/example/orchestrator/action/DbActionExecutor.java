package com.example.orchestrator.action;

import com.example.orchestrator.model.GenericEntity;
import com.example.orchestrator.model.Step;
import com.example.orchestrator.repository.GenericEntityRepository;
import com.example.orchestrator.util.ExecutionContext;
import com.example.orchestrator.util.VariableResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DbActionExecutor implements ActionExecutor {

    private final GenericEntityRepository genericEntityRepository;
    @Autowired
    public DbActionExecutor(GenericEntityRepository genericEntityRepository) {
        this.genericEntityRepository = genericEntityRepository;
    }

    @Override
    public String getType() {
        return "db";
    }

    @Override
    public Object execute(Step step, ExecutionContext context, Map<String, Object> requestParams) {
        if (!"select".equals(step.operation())) {
            throw new UnsupportedOperationException("Operation " + step.operation() + " not supported for db action.");
        }

        String tableName = step.table();

        // Resolve variables in table name
        tableName = VariableResolver.resolveVariables(tableName, requestParams);

        List<GenericEntity> entities = genericEntityRepository.findByTableName(tableName);
        return entities;
    }
}