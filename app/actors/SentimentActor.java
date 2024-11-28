package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import models.services.SentimentService;
import play.libs.concurrent.HttpExecutionContext;

import javax.inject.Inject;

public class SentimentActor extends AbstractActor {

    private final SentimentService sentimentService;
    private final HttpExecutionContext httpExecutionContext;

    @Inject
    public SentimentActor(SentimentService sentimentService, HttpExecutionContext httpExecutionContext) {
        this.sentimentService = sentimentService;
        this.httpExecutionContext = httpExecutionContext;
    }

    // Create Props that allows dependency injection for this actor
    public static Props props(SentimentService sentimentService, HttpExecutionContext httpExecutionContext) {
        return Props.create(SentimentActor.class, () -> new SentimentActor(sentimentService, httpExecutionContext));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, description -> {
                    // Calculate sentiment asynchronously using the injected HttpExecutionContext
                    String sentiment = sentimentService.calculateSentiment(description);
                    // Send the sentiment result back to the sender
                    getSender().tell(sentiment, getSelf());
                })
                .build();
    }
}
