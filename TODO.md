Orchestrator Application Project TODO Checklist


Phase 1: Foundation & Data Modeling (Prompt 1)

[ ] Update pom.xml: Add dependencies for web, validation, data-jpa, json, and test.

[ ] Create Package Structure: controller, service, model, config, action, util.

[ ] Create Model Classes: Define Records/POJOs for:
- Specification
- Input
- InputParameter
- Validation
- Step
- Output
- OutputParameter

[ ] Add Jackson Annotations: Ensure models can be deserialized from JSON.

[ ] Write Model Unit Tests: Create tests for each model class using ObjectMapper to verify JSON parsing.

Phase 2: Basic API Layer (Prompt 2)

[ ] Create OrchestratorController: Implement POST /orchestrate endpoint.

[ ] Create OrchestratorService Interface & OrchestratorServiceImpl: Add stub method.

[ ] Wire Controller to Service: Use constructor injection.

[ ] Write Controller Unit Test (@WebMvcTest): Verify endpoint exists and returns hardcoded response.

[ ] Write Service Unit Test: Basic test for the stub method.

[ ] Manual Test: Run app and test endpoint with curl/Postman.

Phase 3: Specification Loading (Prompt 3)

[ ] Create SpecLoaderService: Implement loadSpec(String product) method.

[ ] Integrate ResourceLoader: Load files from classpath:specs/.

[ ] Create SpecNotFoundException.

[ ] Update OrchestratorServiceImpl: Inject and use SpecLoaderService. Handle SpecNotFoundException by returning a 404 response.

[ ] Write Tests for SpecLoaderService:
- Test successful file load.
- Test exception thrown for missing file.

[ ] Update Controller Test: Mock the spec loading behavior.

Phase 4: Execution Engine & HTTP Action (Prompt 4)

[ ] Create ExecutionContext Class: A wrapper for a Map<String, Object>.

[ ] Create ActionExecutor Interface: With getType() and execute(...) methods.

[ ] Create HttpActionExecutor: Implement executor for type: "http" and method: "GET".

[ ] Create AppConfig: Define RestClient or RestTemplate bean.

[ ] Update OrchestratorServiceImpl:
-  Inject List<ActionExecutor>.
- Implement loop to iterate through steps.
- Find executor for each step and call execute().
- Store results in ExecutionContext.

[ ] Write Test for HttpActionExecutor: Use MockRestServiceServer or @MockBean.

[ ] Write Integration Test for OrchestratorServiceImpl: Mock spec loading and HTTP execution to verify step looping logic.

Phase 5: Variable Substitution (Prompt 5)

[ ] Create VariableResolver Utility Class: With resolveVariables(template, context) method.

[ ] Create VariableNotFoundException.

[ ] Update ActionExecutor Interface: Modify execute() method to accept requestParams for context.

[ ] Update HttpActionExecutor: Apply variable resolution to url and headers using combined context (requestParams + executionContext).

[ ] Update OrchestratorServiceImpl: Pass requestParams to each executor.

[ ] Write Extensive Tests for VariableResolver: Cover no vars, one var, multiple vars, missing vars.

[ ] Update Tests for HttpActionExecutor: Require variable substitution.

Phase 6: Database Action Executor (Prompt 6)

[ ] Create GenericEntity JPA Entity: With id, tableName, and JSON-converted data Map.

[ ] Create MapToStringConverter (if necessary).

[ ] Create GenericEntityRepository: Extends JpaRepository.

[ ] Create DbActionExecutor: Implement for type: "db" and operation: "select".

[ ] Apply Variable Substitution: Resolve variables in the table name.

[ ] Write Test for DbActionExecutor: Use @DataJpaTest with H2. Test data retrieval.

Phase 7: Input Validation (Prompt 7)

[ ] Create InputValidator Class: Implement validate(requestParams, inputSpec) method.

[ ] Implement Validation Checks: Required fields, type checking, min/max, pattern.

[ ] Create InvalidInputException.

[ ] Update OrchestratorServiceImpl: Call validator after loading spec. Handle exception by returning 400 error.

[ ] Write Comprehensive Tests for InputValidator: Cover all validation scenarios.

Phase 8: Error Handling & Retries (Prompt 8)

[ ] Create StepExecutionResult Record: To track each step's outcome.

[ ] Configure RetryTemplate Bean: In AppConfig (3 retries, exponential backoff).

[ ] Update OrchestratorServiceImpl Execution Loop:
- Wrap each executor.execute() call with RetryTemplate.
- Build a List<StepExecutionResult> trace.
- On final failure, halt execution and build error response object.

[ ] Write Integration Tests:
- Test failure after retries (verify error response structure and halted execution).
- Test successful execution with trace.

Phase 9: Formatted Output (Prompt 9)

[ ] Create OutputFormatter Class: Implement formatOutput(outputSpec, context).

[ ] Implement Output Mapping: Extract only the fields specified in the spec's output.parameters from the context.

[ ] Update OrchestratorServiceImpl: On success, call formatter and build final success response.

[ ] Write Tests: Verify success response only contains specified fields.

Phase 10: Production Ready & Polish (Prompt 10)

[ ] Externalize Configuration: Move specs-dir to application.properties.

[ ] Add Logging (@Slf4j): To all major service and executor classes.

[ ] Extend DbActionExecutor: Implement operation: "insert". Apply variable substitution to data map.

[ ] Write Test for DB Insert Operation.

[ ] Create application-prod.properties: For production DB configuration.

[ ] Final End-to-End Test: Create a complex spec (HTTP -> DB -> HTTP), use WireMock for HTTP mocks and H2 for DB. Verify the entire flow.

Testing & Quality Assurance (Ongoing)

[ ] Ensure Unit Test Coverage is high for all core logic (models, services, utils).

[ ] Ensure Integration Tests cover all main success and error scenarios.

[ ] Run mvn clean test successfully before considering any phase complete.

[ ] Perform manual testing with curl/Postman after significant changes.

Documentation & Deployment

[ ] Add API Documentation: Consider Springdoc OpenAPI for auto-generating /v3/api-docs.

[ ] Create sample JSON spec files in src/main/resources/specs/.

[ ] Update README.md with project description, build/run instructions, and API usage examples.

[ ] Configure CI/CD Pipeline (e.g., GitHub Actions) to run tests on git push.