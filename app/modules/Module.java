package modules;

import actors.*;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import models.services.*;
import javax.inject.Named;
import akka.stream.Materializer;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        // No explicit bindings needed here for ActorRefs as we're using @Provides methods
    }

    @Provides
    @Singleton
    @Named("sentimentActor")
    ActorRef provideSentimentActor(ActorSystem system, SentimentService sentimentService) {
        return system.actorOf(SentimentActor.props(sentimentService), "sentimentActor");
    }

    @Provides
    @Singleton
    @Named("channelProfileActor")
    ActorRef provideChannelProfileActor(ActorSystem system, YouTubeService youTubeService) {
        return system.actorOf(ChannelProfileActor.props(youTubeService), "channelProfileActor");
    }

    @Provides
    @Singleton
    @Named("wordStatActor")
    ActorRef provideWordStatActor(ActorSystem system, SearchService searchService) {
        return system.actorOf(WordStatActor.props(searchService), "wordStatActor");
    }

    @Provides
    @Singleton
    @Named("tagActor")
    ActorRef provideTagActor(ActorSystem system, TagsService tagsService) {
        return system.actorOf(TagActor.props(tagsService), "tagActor");
    }

    // Remove the Materializer binding if Play provides it
    // If Play does not provide it, you can also use @Named to differentiate
    @Provides
    @Singleton
    @Named("appMaterializer")
    Materializer provideMaterializer(ActorSystem system) {
        return Materializer.createMaterializer(system);
    }
}
