package com.example.orchestrator.validation;

import com.example.orchestrator.model.Input;
import com.example.orchestrator.model.InputParameter;
import com.example.orchestrator.model.Validation;
import com.example.orchestrator.service.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {

    private InputValidator inputValidator;

    @BeforeEach
    void setUp() {
        inputValidator = new InputValidator();
    }

    @Test
    void validate_noInputSpec_noExceptionThrown() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", "value1");
        inputValidator.validate(requestParams, null);
        // No exception means it passed
    }

    @Test
    void validate_emptyInputSpec_noExceptionThrown() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", "value1");
        Input inputSpec = new Input(Collections.emptyList());
        inputValidator.validate(requestParams, inputSpec);
        // No exception means it passed
    }

    @Test
    void validate_missingRequiredParameter_throwsException() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", "value1");

        InputParameter requiredParam = new InputParameter("requiredParam", "string", true, null);
        Input inputSpec = new Input(List.of(requiredParam));

        InvalidInputException exception = assertThrows(InvalidInputException.class, () ->
                inputValidator.validate(requestParams, inputSpec));
        assertEquals("Missing required parameter: requiredParam", exception.getMessage());
    }

    @Test
    void validate_presentRequiredParameter_noExceptionThrown() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("requiredParam", "value1");

        InputParameter requiredParam = new InputParameter("requiredParam", "string", true, null);
        Input inputSpec = new Input(List.of(requiredParam));

        assertDoesNotThrow(() -> inputValidator.validate(requestParams, inputSpec));
    }

    @Test
    void validate_wrongTypeString_throwsException() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", 123); // Expected string, got integer

        InputParameter param = new InputParameter("param1", "string", false, null);
        Input inputSpec = new Input(List.of(param));

        InvalidInputException exception = assertThrows(InvalidInputException.class, () ->
                inputValidator.validate(requestParams, inputSpec));
        assertEquals("Parameter 'param1' must be a string.", exception.getMessage());
    }

    @Test
    void validate_wrongTypeInteger_throwsException() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", "not_an_integer"); // Expected integer, got string

        InputParameter param = new InputParameter("param1", "integer", false, null);
        Input inputSpec = new Input(List.of(param));

        InvalidInputException exception = assertThrows(InvalidInputException.class, () ->
                inputValidator.validate(requestParams, inputSpec));
        assertEquals("Parameter 'param1' must be an integer.", exception.getMessage());
    }

    @Test
    void validate_wrongTypeNumber_throwsException() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", "not_a_number"); // Expected number, got string

        InputParameter param = new InputParameter("param1", "number", false, null);
        Input inputSpec = new Input(List.of(param));

        InvalidInputException exception = assertThrows(InvalidInputException.class, () ->
                inputValidator.validate(requestParams, inputSpec));
        assertEquals("Parameter 'param1' must be a number.", exception.getMessage());
    }

    @Test
    void validate_wrongTypeBoolean_throwsException() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", "true"); // Expected boolean, got string

        InputParameter param = new InputParameter("param1", "boolean", false, null);
        Input inputSpec = new Input(List.of(param));

        InvalidInputException exception = assertThrows(InvalidInputException.class, () ->
                inputValidator.validate(requestParams, inputSpec));
        assertEquals("Parameter 'param1' must be a boolean.", exception.getMessage());
    }

    @Test
    void validate_stringTooShort_throwsException() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", "abc");

        Validation validation = new Validation(5, null, null, null, null);
        InputParameter param = new InputParameter("param1", "string", false, validation);
        Input inputSpec = new Input(List.of(param));

        InvalidInputException exception = assertThrows(InvalidInputException.class, () ->
                inputValidator.validate(requestParams, inputSpec));
        assertEquals("Parameter 'param1' length must be at least 5", exception.getMessage());
    }

    @Test
    void validate_stringTooLong_throwsException() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", "abcdefg");

        Validation validation = new Validation(null, 5, null, null, null);
        InputParameter param = new InputParameter("param1", "string", false, validation);
        Input inputSpec = new Input(List.of(param));

        InvalidInputException exception = assertThrows(InvalidInputException.class, () ->
                inputValidator.validate(requestParams, inputSpec));
        assertEquals("Parameter 'param1' length must be at most 5", exception.getMessage());
    }

    @Test
    void validate_stringMatchesPattern_noExceptionThrown() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", "abcde");

        Validation validation = new Validation(null, null, "[a-z]{5}", null, null);
        InputParameter param = new InputParameter("param1", "string", false, validation);
        Input inputSpec = new Input(List.of(param));

        assertDoesNotThrow(() -> inputValidator.validate(requestParams, inputSpec));
    }

    @Test
    void validate_stringDoesNotMatchPattern_throwsException() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", "abc12");

        Validation validation = new Validation(null, null, "[a-z]{5}", null, null);
        InputParameter param = new InputParameter("param1", "string", false, validation);
        Input inputSpec = new Input(List.of(param));

        InvalidInputException exception = assertThrows(InvalidInputException.class, () ->
                inputValidator.validate(requestParams, inputSpec));
        assertEquals("Parameter 'param1' does not match the required pattern.", exception.getMessage());
    }

    @Test
    void validate_integerTooLow_throwsException() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", 5);

        Validation validation = new Validation(null, null, null, 10.0, null);
        InputParameter param = new InputParameter("param1", "integer", false, validation);
        Input inputSpec = new Input(List.of(param));

        InvalidInputException exception = assertThrows(InvalidInputException.class, () ->
                inputValidator.validate(requestParams, inputSpec));
        assertEquals("Parameter 'param1' must be at least 10.0", exception.getMessage());
    }

    @Test
    void validate_integerTooHigh_throwsException() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", 15);

        Validation validation = new Validation(null, null, null, null, 10.0);
        InputParameter param = new InputParameter("param1", "integer", false, validation);
        Input inputSpec = new Input(List.of(param));

        InvalidInputException exception = assertThrows(InvalidInputException.class, () ->
                inputValidator.validate(requestParams, inputSpec));
        assertEquals("Parameter 'param1' must be at most 10.0", exception.getMessage());
    }

    @Test
    void validate_numberTooLow_throwsException() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", 5.5);

        Validation validation = new Validation(null, null, null, 10.0, null);
        InputParameter param = new InputParameter("param1", "number", false, validation);
        Input inputSpec = new Input(List.of(param));

        InvalidInputException exception = assertThrows(InvalidInputException.class, () ->
                inputValidator.validate(requestParams, inputSpec));
        assertEquals("Parameter 'param1' must be at least 10.0", exception.getMessage());
    }

    @Test
    void validate_numberTooHigh_throwsException() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", 15.5);

        Validation validation = new Validation(null, null, null, null, 10.0);
        InputParameter param = new InputParameter("param1", "number", false, validation);
        Input inputSpec = new Input(List.of(param));

        InvalidInputException exception = assertThrows(InvalidInputException.class, () ->
                inputValidator.validate(requestParams, inputSpec));
        assertEquals("Parameter 'param1' must be at most 10.0", exception.getMessage());
    }

    @Test
    void validate_multipleParameters_allValid_noExceptionThrown() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", "test");
        requestParams.put("param2", 10);

        InputParameter param1 = new InputParameter("param1", "string", true, new Validation(2, 10, null, null, null));
        InputParameter param2 = new InputParameter("param2", "integer", false, new Validation(null, null, null, 5.0, 15.0));
        Input inputSpec = new Input(List.of(param1, param2));

        assertDoesNotThrow(() -> inputValidator.validate(requestParams, inputSpec));
    }

    @Test
    void validate_multipleParameters_oneInvalid_throwsException() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("param1", "test");
        requestParams.put("param2", 20); // Too high

        InputParameter param1 = new InputParameter("param1", "string", true, new Validation(2, 10, null, null, null));
        InputParameter param2 = new InputParameter("param2", "integer", false, new Validation(null, null, null, 5.0, 15.0));
        Input inputSpec = new Input(List.of(param1, param2));

        InvalidInputException exception = assertThrows(InvalidInputException.class, () ->
                inputValidator.validate(requestParams, inputSpec));
        assertEquals("Parameter 'param2' must be at most 15.0", exception.getMessage());
    }
}