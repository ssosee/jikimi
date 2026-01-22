package com.teuida.jikimi.openai.client.dto.response;

import java.util.List;
import tools.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record CreateEmbeddingsResponse(String object,
                                       Integer index,
                                       List<Double> embedding) {
}
