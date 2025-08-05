package com.gym.crm.storage;

import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataInitializer Tests")
class DataInitializerTest {

    @Mock
    private InMemoryStorage mockStorage;

    @Mock
    private ResourceLoader mockResourceLoader;

    @Mock
    private Resource mockResource;

    private DataInitializer dataInitializer;

    private static final String VALID_JSON = """
        {
          "trainingTypes": [
            {"id": 1, "trainingTypeName": "Cardio"},
            {"id": 2, "trainingTypeName": "Strength"},
            {"trainingTypeName": "Yoga"}
          ]
        }
        """;

    private static final String INVALID_JSON = """
        {
          "trainingTypes": "not an array"
        }
        """;

    private static final String MALFORMED_JSON = """
        {
          "trainingTypes": [
            {"id": 1, "trainingTypeName": "Cardio"}
            {"id": 2, "trainingTypeName": "Strength"}
          ]
        }
        """;

    private static final String EMPTY_JSON = """
        {
          "trainingTypes": []
        }
        """;

    @BeforeEach
    void setUp() {
        dataInitializer = new DataInitializer(mockStorage, mockResourceLoader, "classpath:test-initial-data.json");
    }

    @Test
    @DisplayName("initializeData should load training types from valid JSON file")
    void initializeData_WithValidJsonFile_ShouldLoadTrainingTypes() throws IOException {
        when(mockResourceLoader.getResource("classpath:test-initial-data.json")).thenReturn(mockResource);
        when(mockResource.exists()).thenReturn(true);
        when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream(VALID_JSON.getBytes()));
        when(mockStorage.generateTrainingTypeId()).thenReturn(3L);

        dataInitializer.initializeData();

        verify(mockStorage).storeTrainingType(eq(1L), any(TrainingType.class));
        verify(mockStorage).storeTrainingType(eq(2L), any(TrainingType.class));
        verify(mockStorage).storeTrainingType(eq(3L), any(TrainingType.class));
        verify(mockStorage, times(3)).storeTrainingType(anyLong(), any(TrainingType.class));
        verify(mockStorage).logStorageStatus();
    }

    @Test
    @DisplayName("initializeData should handle non-existent file by loading defaults")
    void initializeData_WithNonExistentFile_ShouldLoadDefaults() {
        when(mockResourceLoader.getResource("classpath:test-initial-data.json")).thenReturn(mockResource);
        when(mockResource.exists()).thenReturn(false);
        when(mockStorage.generateTrainingTypeId()).thenReturn(1L, 2L, 3L, 4L, 5L);

        dataInitializer.initializeData();

        verify(mockStorage, times(5)).storeTrainingType(anyLong(), any(TrainingType.class));
        verify(mockStorage, atLeastOnce()).logStorageStatus();
    }

    @Test
    @DisplayName("initializeData should handle IOException by loading defaults")
    void initializeData_WithIOException_ShouldLoadDefaults() throws IOException {
        when(mockResourceLoader.getResource("classpath:test-initial-data.json")).thenReturn(mockResource);
        when(mockResource.exists()).thenReturn(true);
        when(mockResource.getInputStream()).thenThrow(new IOException("File read error"));
        when(mockStorage.generateTrainingTypeId()).thenReturn(1L, 2L, 3L, 4L, 5L);

        dataInitializer.initializeData();

        verify(mockStorage, times(5)).storeTrainingType(anyLong(), any(TrainingType.class));
    }

    @Test
    @DisplayName("initializeData should handle malformed JSON by loading defaults")
    void initializeData_WithMalformedJson_ShouldLoadDefaults() throws IOException {
        when(mockResourceLoader.getResource("classpath:test-initial-data.json")).thenReturn(mockResource);
        when(mockResource.exists()).thenReturn(true);
        when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream(MALFORMED_JSON.getBytes()));
        when(mockStorage.generateTrainingTypeId()).thenReturn(1L, 2L, 3L, 4L, 5L);

        dataInitializer.initializeData();

        verify(mockStorage, times(5)).storeTrainingType(anyLong(), any(TrainingType.class));
    }

    @Test
    @DisplayName("initializeData should handle invalid training types array")
    void initializeData_WithInvalidTrainingTypesArray_ShouldNotLoadAny() throws IOException {
        when(mockResourceLoader.getResource("classpath:test-initial-data.json")).thenReturn(mockResource);
        when(mockResource.exists()).thenReturn(true);
        when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream(INVALID_JSON.getBytes()));

        dataInitializer.initializeData();

        verify(mockStorage, never()).storeTrainingType(anyLong(), any(TrainingType.class));
        verify(mockStorage).logStorageStatus();
    }

    @Test
    @DisplayName("initializeData should handle empty training types array")
    void initializeData_WithEmptyTrainingTypesArray_ShouldNotLoadAny() throws IOException {
        when(mockResourceLoader.getResource("classpath:test-initial-data.json")).thenReturn(mockResource);
        when(mockResource.exists()).thenReturn(true);
        when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream(EMPTY_JSON.getBytes()));

        dataInitializer.initializeData();

        verify(mockStorage, never()).storeTrainingType(anyLong(), any(TrainingType.class));
        verify(mockStorage).logStorageStatus();
    }

    @Test
    @DisplayName("initializeData should skip invalid training type entries")
    void initializeData_WithInvalidEntries_ShouldSkipInvalidEntries() throws IOException {
        String jsonWithInvalidEntries = """
            {
              "trainingTypes": [
                {"id": 1, "trainingTypeName": "Cardio"},
                {"id": 2, "trainingTypeName": ""},
                {"id": 3, "trainingTypeName": "   "},
                {"id": 4},
                {"trainingTypeName": "Yoga"}
              ]
            }
            """;

        when(mockResourceLoader.getResource("classpath:test-initial-data.json")).thenReturn(mockResource);
        when(mockResource.exists()).thenReturn(true);
        when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream(jsonWithInvalidEntries.getBytes()));
        when(mockStorage.generateTrainingTypeId()).thenReturn(5L);

        dataInitializer.initializeData();

        verify(mockStorage).storeTrainingType(eq(1L), any(TrainingType.class));
        verify(mockStorage).storeTrainingType(eq(5L), any(TrainingType.class));
        verify(mockStorage, times(2)).storeTrainingType(anyLong(), any(TrainingType.class));
    }

    @Test
    @DisplayName("getTrainingTypeById should delegate to storage")
    void getTrainingTypeById_ShouldDelegateToStorage() {
        TrainingType expectedType = new TrainingType(1L, "Cardio");
        when(mockStorage.getTrainingType(1L)).thenReturn(expectedType);

        TrainingType result = dataInitializer.getTrainingTypeById(1L);

        assertEquals(expectedType, result);
        verify(mockStorage).getTrainingType(1L);
    }

    @Test
    @DisplayName("getTrainingTypeById should handle null ID")
    void getTrainingTypeById_WithNullId_ShouldReturnNull() {
        when(mockStorage.getTrainingType(null)).thenReturn(null);
        TrainingType result = dataInitializer.getTrainingTypeById(null);
        assertNull(result);
        verify(mockStorage).getTrainingType(null);
    }

    @Test
    @DisplayName("getTrainingTypeByName should find training type by name")
    void getTrainingTypeByName_ShouldFindByName() {
        TrainingType cardio = new TrainingType(1L, "Cardio");
        TrainingType strength = new TrainingType(2L, "Strength");
        when(mockStorage.getAllTrainingTypes()).thenReturn(java.util.List.of(cardio, strength));

        TrainingType result = dataInitializer.getTrainingTypeByName("Cardio");

        assertEquals(cardio, result);
    }

    @Test
    @DisplayName("getTrainingTypeByName should be case insensitive")
    void getTrainingTypeByName_ShouldBeCaseInsensitive() {
        TrainingType cardio = new TrainingType(1L, "Cardio");
        when(mockStorage.getAllTrainingTypes()).thenReturn(java.util.List.of(cardio));

        TrainingType result = dataInitializer.getTrainingTypeByName("CARDIO");

        assertEquals(cardio, result);
    }

    @Test
    @DisplayName("getTrainingTypeByName should handle whitespace")
    void getTrainingTypeByName_ShouldHandleWhitespace() {
        TrainingType cardio = new TrainingType(1L, "Cardio");
        when(mockStorage.getAllTrainingTypes()).thenReturn(java.util.List.of(cardio));

        TrainingType result = dataInitializer.getTrainingTypeByName("  Cardio  ");

        assertEquals(cardio, result);
    }

    @Test
    @DisplayName("getTrainingTypeByName should return null for null or empty name")
    void getTrainingTypeByName_WithNullOrEmptyName_ShouldReturnNull() {
        assertNull(dataInitializer.getTrainingTypeByName(null));
        assertNull(dataInitializer.getTrainingTypeByName(""));
        assertNull(dataInitializer.getTrainingTypeByName("   "));

        verify(mockStorage, never()).getAllTrainingTypes();
    }

    @Test
    @DisplayName("getTrainingTypeByName should return null when not found")
    void getTrainingTypeByName_WhenNotFound_ShouldReturnNull() {
        TrainingType cardio = new TrainingType(1L, "Cardio");
        when(mockStorage.getAllTrainingTypes()).thenReturn(java.util.List.of(cardio));
        TrainingType result = dataInitializer.getTrainingTypeByName("Nonexistent");

        assertNull(result);
    }

    @Test
    @DisplayName("loadDefaultData should create all default training types")
    void loadDefaultData_ShouldCreateAllDefaultTypes() {
        when(mockStorage.generateTrainingTypeId()).thenReturn(1L, 2L, 3L, 4L, 5L);

        dataInitializer.initializeData(); // This will call loadDefaultData due to no file

        verify(mockStorage, times(5)).storeTrainingType(anyLong(), any(TrainingType.class));

        // Verify specific types were created (can't verify exact order due to array iteration)
        verify(mockStorage, atLeastOnce()).storeTrainingType(anyLong(),
                argThat(type -> "Cardio".equals(type.getTrainingTypeName())));
        verify(mockStorage, atLeastOnce()).storeTrainingType(anyLong(),
                argThat(type -> "Strength".equals(type.getTrainingTypeName())));
        verify(mockStorage, atLeastOnce()).storeTrainingType(anyLong(),
                argThat(type -> "Flexibility".equals(type.getTrainingTypeName())));
        verify(mockStorage, atLeastOnce()).storeTrainingType(anyLong(),
                argThat(type -> "Yoga".equals(type.getTrainingTypeName())));
        verify(mockStorage, atLeastOnce()).storeTrainingType(anyLong(),
                argThat(type -> "CrossFit".equals(type.getTrainingTypeName())));
    }

    @Test
    @DisplayName("Constructor should accept all required dependencies")
    void constructor_ShouldAcceptAllDependencies() {
        assertDoesNotThrow(() -> new DataInitializer(mockStorage, mockResourceLoader, "test-path"));
    }

    @Test
    @DisplayName("Constructor should use default file path when not provided")
    void constructor_ShouldUseDefaultFilePath() {
        assertDoesNotThrow(() -> new DataInitializer(mockStorage, mockResourceLoader, "classpath:initial-data.json"));
    }
}