package com.example.orchestrator.validation;

import com.example.orchestrator.model.Input;
import com.example.orchestrator.model.InputParameter;
import com.example.orchestrator.model.Validation;
import com.example.orchestrator.service.InvalidInputException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Component
public class InputValidator {

    public void validate(Map<String, Object> requestParams, Input inputSpec) {
        if (inputSpec == null || inputSpec.parameters() == null) {
            return;
        }

        for (InputParameter param : inputSpec.parameters()) {
            String paramName = param.name();
            Object paramValue = requestParams.get(paramName);

            // 1. Check if required parameter is present
            if (param.required() && paramValue == null) {
                throw new InvalidInputException("Missing required parameter: " + paramName);
            }

            // Only proceed with type and validation checks if the parameter is present
            if (paramValue != null) {
                // 2. Check parameter type
                validateType(paramName, paramValue, param.type());

                // 3. Perform validations (min, max, etc.)
                if (param.validation() != null) {
                    applyValidations(paramName, paramValue, param.validation(), param.type());
                }
            }
        }
    }

    private void validateType(String paramName, Object paramValue, String expectedType) {
        switch (expectedType.toLowerCase()) {
            case "string":
                if (!(paramValue instanceof String)) {
                    throw new InvalidInputException("Parameter '" + paramName + "' must be a string.");
                }
                break;
            case "integer":
                if (!(paramValue instanceof Integer)) {
                    throw new InvalidInputException("Parameter '" + paramName + "' must be an integer.");
                }
                break;
            case "number": // For doubles/floats
                if (!(paramValue instanceof Number)) {
                    throw new InvalidInputException("Parameter '" + paramName + "' must be a number.");
                }
                break;
            case "boolean":
                if (!(paramValue instanceof Boolean)) {
                    throw new InvalidInputException("Parameter '" + paramName + "' must be a boolean.");
                }
                break;
            default:
                // Unknown type, perhaps log a warning or throw an exception if strict type checking is desired
                break;
        }
    }

    private void applyValidations(String paramName, Object paramValue, Validation validation, String type) {
        if (paramValue == null) {
            return; // No value to validate against
        }

        switch (type.toLowerCase()) {
            case "string":
                String stringValue = (String) paramValue;
                if (validation.minLength() != null && stringValue.length() < validation.minLength()) {
                    throw new InvalidInputException("Parameter '" + paramName + "' length must be at least " + validation.minLength());
                }
                if (validation.maxLength() != null && stringValue.length() > validation.maxLength()) {
                    throw new InvalidInputException("Parameter '" + paramName + "' length must be at most " + validation.maxLength());
                }
                if (validation.pattern() != null && !Pattern.matches(validation.pattern(), stringValue)) {
                    throw new InvalidInputException("Parameter '" + paramName + "' does not match the required pattern.");
                }
                break;
            case "integer":
            case "number":
                Number numberValue = (Number) paramValue;
                if (validation.min() != null && numberValue.doubleValue() < validation.min()) {
                    throw new InvalidInputException("Parameter '" + paramName + "' must be at least " + validation.min());
                }
                if (validation.max() != null && numberValue.doubleValue() > validation.max()) {
                    throw new InvalidInputException("Parameter '" + paramName + "' must be at most " + validation.max());
                }
                break;
            // No specific validations for boolean type yet
        }
    }
}