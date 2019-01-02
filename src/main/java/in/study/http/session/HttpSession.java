package in.study.http.session;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;

public class HttpSession {
    private Map<String, Object> store = Maps.newHashMap();
    private String id;

    public HttpSession(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAttribute(String key, Object value) {
        this.store.put(key, value);
    }

    public Optional<Object> getAttribute(String key) {
        return Optional.ofNullable(this.store.get(key));
    }

    public void removeAttribute(String key) {
        this.store.remove(key);
    }

    public void invalidate() {
        HttpSessions.expireSession(id);
    }
}
