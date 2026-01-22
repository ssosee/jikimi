package com.teuida.jikimi.openai.client;

import com.teuida.jikimi.openai.client.dto.request.CreateEmbeddingsRequest;
import com.teuida.jikimi.openai.client.dto.response.CreateEmbeddingsResponse;
import com.teuida.jikimi.openai.config.OpenAiFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "open-ai-api",
        url = "${open-ai.url}",
        configuration = OpenAiFeignConfig.class
)
public interface OpenAiApiClient {

    /**
     * <a href="https://platform.openai.com/docs/api-reference/embeddings/create">참고</a>
     */
    @PostMapping("/v1/embeddings")
    CreateEmbeddingsResponse createEmbeddings(@RequestBody CreateEmbeddingsRequest request);
}
