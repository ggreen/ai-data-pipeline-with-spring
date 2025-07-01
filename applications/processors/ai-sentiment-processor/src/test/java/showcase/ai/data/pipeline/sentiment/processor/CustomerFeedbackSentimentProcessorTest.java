package showcase.ai.data.pipeline.sentiment.processor;

import nyla.solutions.core.patterns.creational.generator.JavaBeanGeneratorCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import showcase.ai.data.pipeline.sentiment.domains.CustomerFeedback;
import showcase.ai.data.pipeline.sentiment.domains.FeedbackSentiment;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerFeedbackSentimentProcessorTest {

    private CustomerFeedbackSentimentProcessor subject;

    @Mock
    private ChatClient chatClient;

    private final CustomerFeedback customerFeedback = JavaBeanGeneratorCreator.of(CustomerFeedback.class).create();
    @Mock
    private ChatClient.ChatClientRequestSpec prompt;
    @Mock
    private ChatClient.ChatClientRequestSpec user;
    @Mock
    private ChatClient.CallResponseSpec callResponse;

    @BeforeEach
    void setUp() {
        subject = new CustomerFeedbackSentimentProcessor(chatClient);
    }

    @Test
    void positiveSentiment() {

        FeedbackSentiment expected = FeedbackSentiment.builder().customerFeedback(customerFeedback).sentiment(FeedbackSentiment.Sentiment.Positive).build();

        when(chatClient.prompt()).thenReturn(prompt);
        when(prompt.user(any(Consumer.class))).thenReturn(user);
        when(user.call()).thenReturn(callResponse);
        when(callResponse.entity(any(Class.class))).thenReturn(expected.sentiment());

        var actual = subject.apply(customerFeedback);

        assertThat(actual).isEqualTo(expected);
    }
}