package com.teuida.jikimi.domain.issue.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Converter
public class DoubleListJsonConverter implements AttributeConverter<List<Double>, String> {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final TypeReference<List<Double>> TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<Double> attribute) {
        try {
            return attribute == null ? null : OM.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("embeddings 직렬화 실패", e);
        }
    }

    @Override
    public List<Double> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null || dbData.isBlank() ? List.of() : OM.readValue(dbData, TYPE);
        } catch (Exception e) {
            throw new IllegalArgumentException("embeddings 역직렬화 실패", e);
        }
    }
}
