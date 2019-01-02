package in.study.http.session;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class HttpSessions {
    private static Map<String, HttpSession> sessions;

    public static HttpSession getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId)).orElseGet(HttpSessions::registerSession);
    }

    public static HttpSession registerSession() {
        String sessionId = UUID.randomUUID().toString();
        HttpSession session = new HttpSession(sessionId);
        sessions.put(sessionId, session);

        return session;
    }

    public static void expireSession(String sessionId) {
        sessions.remove(sessionId);
    }
}
