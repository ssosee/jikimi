package com.teuida.jikimi.openai.client;

import com.teuida.jikimi.openai.client.dto.request.CreateEmbeddingsRequest;
import com.teuida.jikimi.openai.client.dto.response.CreateEmbeddingsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OpenAiApiClientTest {

    @Autowired
    private OpenAiApiClient openAiApiClient;

    @Test
    @DisplayName("")
    void test() {
        // given

        // when
        CreateEmbeddingsResponse embeddings = openAiApiClient.createEmbeddings(new CreateEmbeddingsRequest("hello world!"));
        // then
        System.out.println(embeddings);
    }
}