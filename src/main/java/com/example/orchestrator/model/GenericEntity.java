package com.example.orchestrator.model;

import com.example.orchestrator.util.MapToStringConverter;

import jakarta.persistence.*;
import java.util.Map;

@Entity
public class GenericEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tableName;

    @Convert(converter = MapToStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, String> data;

    public GenericEntity(String tableName, Map<String, String> data) {
        this.tableName = tableName;
        this.data = data;
    }

    public GenericEntity() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}