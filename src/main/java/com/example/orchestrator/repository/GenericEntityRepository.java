package com.example.orchestrator.repository;

import com.example.orchestrator.model.GenericEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenericEntityRepository extends JpaRepository<GenericEntity, Long> {
    List<GenericEntity> findByTableName(String tableName);
}