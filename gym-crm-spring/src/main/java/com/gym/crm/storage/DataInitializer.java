package com.gym.crm.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final InMemoryStorage storage;
    private final ResourceLoader resourceLoader;
    private final String dataFilePath;
    private final ObjectMapper objectMapper;


    public DataInitializer(InMemoryStorage storage,
                           ResourceLoader resourceLoader,
                           @Value("${data.file.path:classpath:initial-data.json}") String dataFilePath) {
        this.storage = storage;
        this.resourceLoader = resourceLoader;
        this.dataFilePath = dataFilePath;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void initializeData() {
        logger.info("Starting data initialization from: {}", dataFilePath);

        try {
            loadDataFromFile();
            storage.logStorageStatus();
            logger.info("Data initialization completed successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize data from file: {}", dataFilePath, e);
            loadDefaultData();
        }
    }

    private void loadDataFromFile() throws IOException {
        Resource resource = resourceLoader.getResource(dataFilePath);

        if (!resource.exists()) {
            logger.warn("Data file not found: {}. Loading default data instead.", dataFilePath);
            loadDefaultData();
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode rootNode = objectMapper.readTree(inputStream);

            if (rootNode.has("trainingTypes")) {
                loadTrainingTypes(rootNode.get("trainingTypes"));
            }
        }
    }

    private void loadTrainingTypes(JsonNode trainingTypesNode) {
        if (!trainingTypesNode.isArray()) {
            logger.warn("trainingTypes node is not an array, skipping");
            return;
        }

        int count = 0;
        for (JsonNode typeNode : trainingTypesNode) {
            try {
                Long id = typeNode.has("id") ? typeNode.get("id").asLong() : null;
                String name = typeNode.has("trainingTypeName") ?
                        typeNode.get("trainingTypeName").asText() : null;

                if (name != null && !name.trim().isEmpty()) {
                    TrainingType trainingType = new TrainingType(name.trim());

                    if (id != null) {
                        trainingType.setId(id);
                        storage.storeTrainingType(id, trainingType);
                    } else {
                        Long generatedId = storage.generateTrainingTypeId();
                        trainingType.setId(generatedId);
                        storage.storeTrainingType(generatedId, trainingType);
                    }

                    count++;
                    logger.debug("Loaded training type: {} with ID: {}", name, trainingType.getId());
                }
            } catch (Exception e) {
                logger.warn("Failed to load training type from node: {}", typeNode, e);
            }
        }

        logger.info("Loaded {} training types from file", count);
    }

    private void loadDefaultData() {
        logger.info("Loading default training types");

        String[] defaultTypes = {"Cardio", "Strength", "Flexibility", "Yoga", "CrossFit"};

        for (String typeName : defaultTypes) {
            TrainingType trainingType = new TrainingType(typeName);
            Long id = storage.generateTrainingTypeId();
            trainingType.setId(id);
            storage.storeTrainingType(id, trainingType);
            logger.debug("Created default training type: {} with ID: {}", typeName, id);
        }

        logger.info("Loaded {} default training types", defaultTypes.length);
    }

    public TrainingType getTrainingTypeById(Long id) {
        return storage.getTrainingType(id);
    }

    public TrainingType getTrainingTypeByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        return storage.getAllTrainingTypes().stream()
                .filter(tt -> name.trim().equalsIgnoreCase(tt.getTrainingTypeName()))
                .findFirst()
                .orElse(null);
    }
}