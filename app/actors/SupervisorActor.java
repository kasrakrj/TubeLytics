
package actors;

import akka.actor.*;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class SupervisorActor extends AbstractActor {


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

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(
                10,
                Duration.create(1, TimeUnit.MINUTES),
                throwable -> SupervisorStrategy.restart()
        );
    }

    public static Props props() {
        return Props.create(SupervisorActor.class, SupervisorActor::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Props.class, props -> {
                    ActorRef child = getContext().actorOf(props);
                    sender().tell(child, self());
                })
                .build();
    }
}
