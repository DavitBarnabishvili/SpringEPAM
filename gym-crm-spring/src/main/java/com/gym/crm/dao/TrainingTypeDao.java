package com.gym.crm.dao;

import com.gym.crm.entity.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeDao {

    TrainingType create(TrainingType trainingType);

    Optional<TrainingType> findById(Long id);

    Optional<TrainingType> findByName(String name);

    List<TrainingType> findAll();

    boolean existsById(Long id);

    boolean existsByName(String name);

    long count();
}