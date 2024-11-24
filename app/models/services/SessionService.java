package models.services;

import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.ConcurrentHashMap;

public class SessionService {

    private static final ConcurrentHashMap<Http.Request, String> sessionMap = new ConcurrentHashMap<>();

    public static String getSessionId(Http.Request request) {
        String sessionID = request.session().getOptional("sessionId").orElse(null);
        if (sessionID == null) {
            if (!sessionMap.containsKey(request)) {
                sessionID = generateSessionID();
                sessionMap.put(request, sessionID);
            }else {
                sessionID = sessionMap.get(request);
            }
        }
        return sessionID;
    }

    public static boolean hasSessionId(Http.Request request) {
        return request.session().getOptional("sessionId").isPresent();
    }

    public static String getSessionIdByHeader(Http.RequestHeader requestHeader) {
        return requestHeader.session().getOptional("sessionId").orElse(null);
    }

    private static String generateSessionID() {
        return java.util.UUID.randomUUID().toString();
    }

    public static Result addSessionId(Http.Request request, Result result) {
        if (!hasSessionId(request)){
            String sessionID = getSessionId(request);
            return result.addingToSession(request, "sessionId", sessionID);
        }else {
            return result;
        }
    }


}
