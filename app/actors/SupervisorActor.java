package actors;

import akka.actor.*;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * The {@code SupervisorActor} class is an Akka actor responsible for supervising child actors and periodically
 * triggering actions, such as fetching videos. It uses a one-for-one supervision strategy to manage child actor failures.
 */
public class SupervisorActor extends AbstractActor {

    /**
     * Initializes the {@code SupervisorActor}.
     * This method is called when the actor is started. It schedules a periodic task to send "FetchVideos" messages to itself every 30 seconds.
     */
    @Override
    public void preStart() {
        getContext().getSystem().scheduler().scheduleWithFixedDelay(
                Duration.create(0, TimeUnit.SECONDS),  // Initial delay
                Duration.create(30, TimeUnit.SECONDS), // Fetch every 30 seconds
                self(),
                "FetchVideos",
                getContext().getSystem().dispatcher(),
                self()
        );
    }

    /**
     * Defines the supervision strategy for this actor.
     * It uses a one-for-one strategy that restarts the failing child actor. The strategy allows up to 10 restarts within a 1-minute window.
     *
     * @return The supervision strategy.
     */
    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(
                10,
                Duration.create(1, TimeUnit.MINUTES),
                throwable -> SupervisorStrategy.restart()
        );
    }

    /**
     * Factory method for creating {@code Props} for this actor.
     *
     * @return The {@code Props} object for creating {@code SupervisorActor} instances.
     */
    public static Props props() {
        return Props.create(SupervisorActor.class, SupervisorActor::new);
    }

    /**
     * Defines the message handling behavior of the {@code SupervisorActor}.
     * This actor creates and supervises child actors upon receiving a {@code Props} message.
     *
     * @return The {@code Receive} object defining message handling behavior.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Props.class, props -> {
                    // Create a child actor using the received Props and reply with the reference.
                    ActorRef child = getContext().actorOf(props);
                    sender().tell(child, self());
                })
                .build();
    }
}
