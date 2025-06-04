package showcase.ai.data.pipeline.postgres.embedding.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nyla.solutions.core.patterns.conversion.Converter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;
import showcase.ai.data.pipeline.postgres.embedding.properties.EmbeddingSimilarityProperties;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.List.of;

/**
 *
 * Save payload as document and search for similarities
 * @author Gregory Green
 */
@Component
@Slf4j
public class EmbeddingSimilarityFunction implements Function<String,List<Document> > {
    private final VectorStore vectorStore;
    private final EmbeddingSimilarityProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Converter<String, Document> converter;

    public EmbeddingSimilarityFunction(VectorStore vectorStore, EmbeddingSimilarityProperties properties, Converter<String, Document> converter) {
        this.vectorStore = vectorStore;
        this.properties = properties;
        this.converter = converter;
    }

    @Override
    public List<Document> apply(String payload) {

        log.info("payload: {}: properties: {}", payload, properties);
        var payloadDocument = converter.convert(payload);

        log.info("payloadDocument: {}", payloadDocument);

        var text = payloadDocument.getText();
        if (text == null)
            return null;

        log.info("Saving into vector store");
        vectorStore.add(of(payloadDocument));


        var criteria = SearchRequest.builder().query(payload)
                .topK(properties.getTopK())
//                .filterExpression(
//                        new FilterExpressionBuilder()
//                                .ne("email", payloadDocument.getId()).build())
                .query(text)
                .similarityThreshold(properties.getSimilarityThreshold())
                .build();

        log.info("Searching criteria: {}",criteria);

        var similarities = vectorStore.similaritySearch(criteria);

        log.info("similarities: {}", similarities);

        return filter(payloadDocument, similarities);

    }

    /**
     *
     * @param payloadDocument the payloadDocument
     * @param similarities
     * @return
     */
    List<Document> filter(Document payloadDocument, List<Document> similarities){

        if(similarities == null || similarities.isEmpty())
            return null;

        List<Document> filtered =  similarities.stream()
                .filter(resultDoc -> !resultDoc.getId().equals(payloadDocument.getId()))
                .toList();

        return !filtered.isEmpty() ? filtered : null;
    }
}
