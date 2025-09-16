package com.example.orchestrator;

import com.example.orchestrator.model.GenericEntity;
import com.example.orchestrator.repository.GenericEntityRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.springframework.test.annotation.DirtiesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;

@SpringBootTest(classes = JsonOrchestratorApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrchestratorE2EIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GenericEntityRepository genericEntityRepository;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() throws InterruptedException {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8080);
        genericEntityRepository.deleteAll(); // Clear H2 database before each test
        Thread.sleep(1000);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void endToEndOrchestration_shouldWorkCorrectly() throws Exception {
        // 1. Mock HTTP GET for product details
        stubFor(get(urlEqualTo("/products/P123"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"id\": \"P123\", \"name\": \"Laptop\", \"description\": \"Powerful laptop\", \"price\": 1200.00}")));

        // 2. Mock HTTP POST for product updates
        stubFor(WireMock.post(urlEqualTo("/product-updates"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.productId", equalTo("P123")))
                .withRequestBody(matchingJsonPath("$.status", equalTo("inserted")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"message\": \"Product update recorded\"}")));

        // Prepare request parameters
        String requestBody = "{\"product\": \"e2eTestProduct\", \"productId\": \"P123\", \"productName\": \"Laptop\", \"productPrice\": 1200.00}";

        // Execute orchestration
        MvcResult result = mockMvc.perform(post("/api/orchestrate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.output.finalStatus").value("Orchestration completed successfully for product Laptop"))
                .andExpect(jsonPath("$.output.productDetails.id").value("P123"))
                .andExpect(jsonPath("$.output.dbResult.tableName").value("products"))
                .andExpect(jsonPath("$.output.dbResult.data.name").value("Laptop"))
                .andExpect(jsonPath("$.output.postResult.message").value("Product update recorded"))
                .andReturn();

        // Verify DB insert
        List<GenericEntity> productsInDb = genericEntityRepository.findByTableName("products");
        assertFalse(productsInDb.isEmpty());
        assertEquals(1, productsInDb.size());
        GenericEntity savedProduct = productsInDb.get(0);
        assertEquals("P123", savedProduct.getData().get("id"));
        assertEquals("Laptop", savedProduct.getData().get("name"));
        assertEquals(1200.00, savedProduct.getData().get("price"));
        assertEquals("Powerful laptop", savedProduct.getData().get("description"));

        // Verify WireMock interactions
        verify(1, getRequestedFor(urlEqualTo("/products/P123")));
        verify(1, WireMock.postRequestedFor(urlEqualTo("/product-updates"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.productId", equalTo("P123")))
                .withRequestBody(matchingJsonPath("$.status", equalTo("inserted"))));
    }
}