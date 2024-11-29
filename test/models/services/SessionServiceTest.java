package models.services;

import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.test.Helpers;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class SessionServiceTest {

    private Http.RequestBuilder requestBuilder;
    private ConcurrentHashMap<Http.Request, String> sessionMap;

    @Before
    public void setUp() {
        sessionMap = new ConcurrentHashMap<>();
        requestBuilder = Helpers.fakeRequest();
    }

    @Test
    public void testGetSessionId_NewSession() {
        Http.Request request = requestBuilder.build();

        String sessionId = SessionService.getSessionId(request);

        assertNotNull("Session ID should be generated for a new session.", sessionId);

        // Ensure the session ID is stored in the session map
        String storedSessionId = SessionService.getSessionId(request);
        assertEquals("Generated Session ID should be stored in the map.", sessionId, storedSessionId);
    }

    @Test
    public void testGetSessionId_ExistingSession() {
        String existingSessionId = "existing-session-id";
        requestBuilder.session("sessionId", existingSessionId);
        Http.Request request = requestBuilder.build();

        String sessionId = SessionService.getSessionId(request);

        assertEquals("Existing session ID should be returned.", existingSessionId, sessionId);
    }

    @Test
    public void testHasSessionId_True() {
        requestBuilder.session("sessionId", "some-session-id");
        Http.Request request = requestBuilder.build();

        assertTrue("hasSessionId should return true when sessionId exists.",
                SessionService.hasSessionId(request));
    }

    @Test
    public void testHasSessionId_False() {
        Http.Request request = requestBuilder.build();

        assertFalse("hasSessionId should return false when sessionId is not present.",
                SessionService.hasSessionId(request));
    }

    @Test
    public void testGetSessionIdByHeader() {
        String headerSessionId = "header-session-id";
        requestBuilder.session("sessionId", headerSessionId);
        Http.RequestHeader requestHeader = requestBuilder.build();

        String sessionId = SessionService.getSessionIdByHeader(requestHeader);

        assertEquals("Session ID from the header should be returned.", headerSessionId, sessionId);
    }

    @Test
    public void testAddSessionId_NewSession() {
        Http.Request request = requestBuilder.build();
        Result result = Results.ok();

        Result resultWithSession = SessionService.addSessionId(request, result);

        assertNotNull("Result with added session ID should not be null.", resultWithSession);
        String sessionId = resultWithSession.session().get("sessionId").orElse(null);
        assertNotNull("Session ID should be added to the result.", sessionId);
    }

}
