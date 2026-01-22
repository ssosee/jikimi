package com.teuida.jikimi.openai.client.dto.request;

public record CreateEmbeddingsRequest(String input,
                                      String model) {

    public CreateEmbeddingsRequest(String input) {
        this(input, "text-embedding-3-small");
    }
}
