package com.teuida.jikimi.openai.client.dto.response;

import java.util.List;
import lombok.Builder;
import tools.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import tools.jackson.databind.annotation.JsonNaming;

@Builder
@JsonNaming(SnakeCaseStrategy.class)
public record OpenAiResponse<T>(String object,
                                List<T> data,
                                String model,
                                Usage usage) {

    public static <T> OpenAiResponse<T> of(String object, List<T> data, String model, Usage usage) {
        return OpenAiResponse.<T>builder()
                .object(object)
                .data(data)
                .model(model)
                .usage(usage)
                .build();
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record Usage(Integer promptTokens,
                        Integer totalTokens) {
    }
}
