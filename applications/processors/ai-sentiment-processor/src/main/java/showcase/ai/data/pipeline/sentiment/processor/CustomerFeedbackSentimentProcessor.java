package showcase.ai.data.pipeline.sentiment.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import showcase.ai.data.pipeline.sentiment.domains.CustomerFeedback;
import showcase.ai.data.pipeline.sentiment.domains.FeedbackSentiment;
import java.util.function.Function;

/**
 * Determine the sentiment based on customer feedback
 * @author Gregory Green
 */
@Component
@RequiredArgsConstructor
public class CustomerFeedbackSentimentProcessor implements Function<CustomerFeedback, FeedbackSentiment> {
    private final ChatClient chatClient;

    private final String prompt = """
            Analyze the sentiment of this text: "{text}".
            Respond with only one word: Positive, Neutral, or Negative.
            """;



    @Override
    public FeedbackSentiment apply(CustomerFeedback customerFeedback) {

        var sentiment = chatClient.prompt()
                .user(u -> u.text(prompt)
                        .param("text", customerFeedback.feedback()))
                .call()
                .entity(FeedbackSentiment.Sentiment.class);

        return FeedbackSentiment.builder()
                .customerFeedback(customerFeedback)
                .sentiment(sentiment).build();
    }
}
