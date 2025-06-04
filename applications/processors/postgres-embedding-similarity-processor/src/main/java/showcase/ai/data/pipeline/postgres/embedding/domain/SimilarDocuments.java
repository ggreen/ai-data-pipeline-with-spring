package showcase.ai.data.pipeline.postgres.embedding.domain;

import org.springframework.ai.document.Document;

import java.util.List;

public record SimilarDocuments(String id, List<Document> similarities) {
}
