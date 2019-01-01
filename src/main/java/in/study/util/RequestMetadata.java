package in.study.util;

import in.study.http.HttpMethod;

import java.util.Objects;

public class RequestMetadata {
    private HttpMethod method;
    private String path;

    public RequestMetadata(HttpMethod method, String path) {
        this.method = method;
        this.path = path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestMetadata that = (RequestMetadata) o;
        return method == that.method &&
                Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path);
    }
}
