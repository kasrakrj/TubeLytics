package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import models.entities.Video;
import models.services.TagsService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TagActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("TagActorTestSystem");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system, Duration.create("10 seconds"), false);
        system = null;
    }

    @Test
    public void testHandleGetVideoSuccess() {
        new TestKit(system) {{
            // Arrange
            TagsService mockTagsService = mock(TagsService.class);
            String videoId = "video123";
            Video mockVideo = new Video(
                    "Sample Title",
                    "Sample Description",
                    "Sample Channel",
                    "http://thumbnail.url",
                    videoId,
                    "channel123",
                    "http://video.url",
                    "2023-01-01T00:00:00Z"
            );

            when(mockTagsService.getVideoByVideoId(videoId))
                    .thenReturn(CompletableFuture.completedFuture(mockVideo));

            ActorRef tagActor = system.actorOf(TagActor.props(mockTagsService));

            // Act
            tagActor.tell(new TagMessages.GetVideo(videoId), getRef());

            // Assert
            TagMessages.GetVideoResponse response = expectMsgClass(TagMessages.GetVideoResponse.class);
            assertEquals(mockVideo, response.getVideo());
        }};
    }

    @Test
    public void testHandleGetVideoFailure() {
        new TestKit(system) {{
            // Arrange
            TagsService mockTagsService = mock(TagsService.class);
            String videoId = "video123";
            String errorMessage = "Video not found";

            when(mockTagsService.getVideoByVideoId(videoId))
                    .thenReturn(CompletableFuture.failedFuture(new Exception(errorMessage)));

            ActorRef tagActor = system.actorOf(TagActor.props(mockTagsService));

            // Act
            tagActor.tell(new TagMessages.GetVideo(videoId), getRef());

            // Assert
            TagMessages.TagsError response = expectMsgClass(TagMessages.TagsError.class);
            assertEquals(errorMessage, response.getErrorMessage());
        }};
    }

    @Test
    public void testHandleGetTagsSuccess() {
        new TestKit(system) {{
            // Arrange
            TagsService mockTagsService = mock(TagsService.class);
            String videoId = "video123";
            List<String> mockTags = Arrays.asList("tag1", "tag2");

            when(mockTagsService.getTagsByVideoId(videoId))
                    .thenReturn(CompletableFuture.completedFuture(mockTags));

            ActorRef tagActor = system.actorOf(TagActor.props(mockTagsService));

            // Act
            tagActor.tell(new TagMessages.GetTags(videoId), getRef());

            // Assert
            TagMessages.GetTagsResponse response = expectMsgClass(TagMessages.GetTagsResponse.class);
            assertEquals(mockTags, response.getTags());
        }};
    }

    @Test
    public void testHandleGetTagsFailure() {
        new TestKit(system) {{
            // Arrange
            TagsService mockTagsService = mock(TagsService.class);
            String videoId = "video123";
            String errorMessage = "Tags not found";

            when(mockTagsService.getTagsByVideoId(videoId))
                    .thenReturn(CompletableFuture.failedFuture(new Exception(errorMessage)));

            ActorRef tagActor = system.actorOf(TagActor.props(mockTagsService));

            // Act
            tagActor.tell(new TagMessages.GetTags(videoId), getRef());

            // Assert
            TagMessages.TagsError response = expectMsgClass(TagMessages.TagsError.class);
            assertEquals(errorMessage, response.getErrorMessage());
        }};
    }
}
