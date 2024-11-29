package models.services;

import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.test.Helpers;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link SessionService} class.
 *
 * This test class verifies the behavior of session management functionality, including:
 * - Generating and retrieving session IDs.
 * - Checking for the presence of session IDs in requests.
 * - Adding session IDs to HTTP responses.
 *
 * The tests use Play Framework's {@code Http.Request} and {@code Http.RequestHeader} for
 * simulating HTTP requests and sessions.
 */
public class SessionServiceTest {

    private Http.RequestBuilder requestBuilder;

    @Before
    public void setUp() {
        requestBuilder = Helpers.fakeRequest();
    }

    /**
     * Tests the {@code getSessionId} method to ensure a new session ID is generated
     * for a request without an existing session.
     *
     * Validates:
     * - A session ID is generated.
     * - The generated session ID is stored in the session map.
     */
    @Test
    public void testGetSessionId_NewSession() {
        Http.Request request = requestBuilder.build();

        String sessionId = SessionService.getSessionId(request);

        assertNotNull("Session ID should be generated for a new session.", sessionId);

        // Ensure the session ID is stored in the session map
        String storedSessionId = SessionService.getSessionId(request);
        assertEquals("Generated Session ID should be stored in the map.", sessionId, storedSessionId);
    }

    /**
     * Tests the {@code getSessionId} method to ensure an existing session ID is returned
     * for a request that already has a session ID.
     *
     * Validates:
     * - The returned session ID matches the one present in the request.
     */
    @Test
    public void testGetSessionId_ExistingSession() {
        String existingSessionId = "existing-session-id";
        requestBuilder.session("sessionId", existingSessionId);
        Http.Request request = requestBuilder.build();

        String sessionId = SessionService.getSessionId(request);

        assertEquals("Existing session ID should be returned.", existingSessionId, sessionId);
    }

    /**
     * Tests the {@code hasSessionId} method to verify that it returns {@code true}
     * when a session ID is present in the request.
     */
    @Test
    public void testHasSessionId_True() {
        requestBuilder.session("sessionId", "some-session-id");
        Http.Request request = requestBuilder.build();

        assertTrue("hasSessionId should return true when sessionId exists.",
                SessionService.hasSessionId(request));
    }

    /**
     * Tests the {@code hasSessionId} method to verify that it returns {@code false}
     * when a session ID is not present in the request.
     */
    @Test
    public void testHasSessionId_False() {
        Http.Request request = requestBuilder.build();

        assertFalse("hasSessionId should return false when sessionId is not present.",
                SessionService.hasSessionId(request));
    }

    /**
     * Tests the {@code getSessionIdByHeader} method to ensure it retrieves the session ID
     * from the request headers when available.
     *
     * Validates:
     * - The session ID in the header matches the expected value.
     */
    @Test
    public void testGetSessionIdByHeader() {
        String headerSessionId = "header-session-id";
        requestBuilder.session("sessionId", headerSessionId);
        Http.RequestHeader requestHeader = requestBuilder.build();

        String sessionId = SessionService.getSessionIdByHeader(requestHeader);

        assertEquals("Session ID from the header should be returned.", headerSessionId, sessionId);
    }

    /**
     * Tests the {@code addSessionId} method to ensure a new session ID is added
     * to the HTTP response when no session ID exists in the request.
     *
     * Validates:
     * - A session ID is added to the response.
     * - The session ID is not null.
     */
    @Test
    public void testAddSessionId_NewSession() {
        Http.Request request = requestBuilder.build();
        Result result = Results.ok();

        Result resultWithSession = SessionService.addSessionId(request, result);

        assertNotNull("Result with added session ID should not be null.", resultWithSession);
        String sessionId = resultWithSession.session().get("sessionId").orElse(null);
        assertNotNull("Session ID should be added to the result.", sessionId);
    }

    /**
     * Tests the {@code addSessionId} method to verify it does not overwrite
     * an existing session ID in the request.
     *
     * Validates:
     * - The existing session ID remains unchanged.
     * - No new session ID is added.
     */
    @Test
    public void testAddSessionId_ExistingSession() {
        // Build the request with a session containing "sessionId"
        Http.Request request = Helpers.fakeRequest()
                .session("sessionId", "existing-session-id")
                .build();

        Result result = Results.ok();

        Result updatedResult = SessionService.addSessionId(request, result);


        // It should be null since the session ID is not added again
        assertNull(updatedResult.session());
    }
}
