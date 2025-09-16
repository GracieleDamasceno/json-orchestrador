package com.example.orchestrator.service;

import com.example.orchestrator.model.Input;
import com.example.orchestrator.model.Output;
import com.example.orchestrator.model.Specification;
import com.example.orchestrator.model.Step;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpecLoaderServiceTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private ObjectMapper objectMapper;

    private SpecLoaderServiceImpl specLoaderService;

    private static final String SPECS_DIRECTORY = "classpath:specs/";

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(resourceLoader, objectMapper);
        specLoaderService = new SpecLoaderServiceImpl(resourceLoader, objectMapper, SPECS_DIRECTORY);
    }

    @Test
    void loadSpec_success() throws IOException {
        String product = "testProduct";
        String resourcePath = SPECS_DIRECTORY + product + ".json";
        String jsonContent = "{ \"name\": \"testProduct\", \"description\": \"A test product specification\", \"inputs\": [], \"outputs\": [], \"steps\": [] }";
        Resource mockResource = mock(Resource.class);
        // Create dummy Input, Output, and Steps for the Specification record
        Input dummyInput = new com.example.orchestrator.model.Input(List.of());
        Output dummyOutput = new com.example.orchestrator.model.Output(List.of());
        List<com.example.orchestrator.model.Step> dummySteps = List.of();

        Specification expectedSpecification = new Specification("testProduct", "A test product specification", dummyInput, dummySteps, dummyOutput);

        when(resourceLoader.getResource(resourcePath)).thenReturn(mockResource);
        when(mockResource.exists()).thenReturn(true);
        when(mockResource.getInputStream()).thenReturn(new ByteArrayResource(jsonContent.getBytes()).getInputStream());
        when(objectMapper.readValue(any(InputStream.class), eq(Specification.class))).thenReturn(expectedSpecification);

        Specification actualSpecification = specLoaderService.loadSpec(product);

        assertNotNull(actualSpecification);
        assertEquals(expectedSpecification.name(), actualSpecification.name());
        assertEquals(expectedSpecification.description(), actualSpecification.description());
        verify(resourceLoader, times(1)).getResource(resourcePath);
        verify(mockResource, times(1)).exists();
        verify(objectMapper, times(1)).readValue(any(InputStream.class), eq(Specification.class));
    }

    @Test
    void loadSpec_notFound() throws IOException {
        String product = "nonExistentProduct";
        String resourcePath = SPECS_DIRECTORY + product + ".json";
        Resource mockResource = mock(Resource.class);

        when(resourceLoader.getResource(resourcePath)).thenReturn(mockResource);
        when(mockResource.exists()).thenReturn(false);

        SpecNotFoundException exception = assertThrows(SpecNotFoundException.class, () -> {
            specLoaderService.loadSpec(product);
        });

        assertEquals("Specification for product 'nonExistentProduct' not found.", exception.getMessage());
        verify(resourceLoader, times(1)).getResource(resourcePath);
        verify(mockResource, times(1)).exists();
        verifyNoInteractions(objectMapper);
    }

    @Test
    void loadSpec_ioException() throws IOException {
        String product = "invalidJsonProduct";
        String resourcePath = SPECS_DIRECTORY + product + ".json";
        Resource mockResource = mock(Resource.class);

        when(resourceLoader.getResource(resourcePath)).thenReturn(mockResource);
        when(mockResource.exists()).thenReturn(true);
        when(mockResource.getInputStream()).thenReturn(new ByteArrayResource("invalid json".getBytes()).getInputStream());
        when(objectMapper.readValue(any(InputStream.class), eq(Specification.class))).thenThrow(new IOException("Error parsing JSON"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            specLoaderService.loadSpec(product);
        });

        assertTrue(exception.getMessage().contains("Failed to parse specification for product 'invalidJsonProduct'"));
        assertTrue(exception.getCause() instanceof IOException);
        verify(resourceLoader, times(1)).getResource(resourcePath);
        verify(mockResource, times(1)).exists();
        verify(objectMapper, times(1)).readValue(any(InputStream.class), eq(Specification.class));
    }
}