package com.teuida.jikimi.domain.issue.entity.embedded;

import com.teuida.jikimi.domain.issue.entity.converter.DoubleListJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

/**
 * OpenAI 임베딩 벡터를 저장하는 Embeddable 클래스
 * <p>
 * 지원 모델: - text-embedding-3-small (1536 차원) - text-embedding-3-large (3072 차원) - text-embedding-ada-002 (1536 차원)
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Getter
public class Embeddings {

    @Column(name = "embedding_dimension", nullable = false)
    private int dimension;

    @Column(name = "embeddings", columnDefinition = "text", nullable = false)
    @Convert(converter = DoubleListJsonConverter.class)
    private List<Double> embeddings;

    public Embeddings(List<Double> values) {
        validateEmbeddings(values);
        this.dimension = values.size();
        this.embeddings = List.copyOf(values);
    }

    private void validateEmbeddings(List<Double> values) {
        Assert.notEmpty(values, () -> "임베딩 벡터는 비어있을 수 없습니다.");
        Assert.noNullElements(values, () -> "임베딩 벡터에 null 값이 포함될 수 없습니다.");
    }

    /**
     * 불변 복사본 반환
     */
    public List<Double> getValues() {
        return embeddings != null ? List.copyOf(embeddings) : List.of();
    }

    /**
     * 코사인 유사도 계산 (1.0에 가까울수록 유사)
     */
    public double cosineSimilarity(Embeddings other) {
        Assert.notNull(other, () -> "비교 대상 임베딩은 null일 수 없습니다.");
        Assert.isTrue(this.dimension == other.dimension,
                () -> String.format("임베딩 차원 불일치: %d != %d", this.dimension, other.dimension));

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < this.dimension; i++) {
            double a = this.embeddings.get(i);
            double b = other.embeddings.get(i);

            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator == 0.0 ? 0.0 : dotProduct / denominator;
    }

    /**
     * 유클리드 거리 계산 (0에 가까울수록 유사)
     */
    public double euclideanDistance(Embeddings other) {
        Assert.notNull(other, () -> "비교 대상 임베딩은 null일 수 없습니다.");
        Assert.isTrue(this.dimension == other.dimension,
                () -> String.format("임베딩 차원 불일치: %d != %d", this.dimension, other.dimension));

        double sum = 0.0;
        for (int i = 0; i < this.dimension; i++) {
            double diff = this.embeddings.get(i) - other.embeddings.get(i);
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    /**
     * 벡터의 L2 norm 반환
     */
    public double norm() {
        double sum = 0.0;
        for (Double value : embeddings) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }

    /**
     * 정규화된 벡터 반환 (OpenAI 임베딩은 이미 정규화되어 있음)
     */
    public Embeddings normalize() {
        double n = norm();
        if (n == 0.0 || Math.abs(n - 1.0) < 1e-10) {
            return this;
        }

        List<Double> normalized = embeddings.stream()
                .map(v -> v / n)
                .toList();

        return new Embeddings(normalized);
    }
}