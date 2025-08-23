package com.gym.crm.data;

import com.gym.crm.dao.TrainingTypeDao;
import com.gym.crm.entity.TrainingType;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final TrainingTypeDao trainingTypeDao;

    @Autowired
    public DataInitializer(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @PostConstruct
    @Transactional
    public void initializeTrainingTypes() {
        logger.info("Initializing constant training types.");

        if (trainingTypeDao.count() == 0) {
            String[] defaultTypes = {"Cardio", "Strength", "Flexibility", "Yoga", "CrossFit"};
            for (String typeName : defaultTypes) {
                TrainingType trainingType = new TrainingType();
                trainingType.setTrainingTypeName(typeName);
                trainingTypeDao.create(trainingType);
                logger.debug("Persisted default training type: {}", typeName);
            }
            logger.info("Successfully initialized {} default training types.", defaultTypes.length);
        } else {
            logger.info("Training types table is already populated. Skipping initialization.");
        }
    }
}