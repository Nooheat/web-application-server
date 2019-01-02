package in.study.http.request;

import java.util.Arrays;
import java.util.Optional;

public enum HttpMethod {
    POST,
    GET,
    PUT,
    DELETE,
    PATCH,
    HEAD;

    public static Optional<HttpMethod> valueFrom(String method) {
        return Arrays.stream(values()).filter(httpMethod -> httpMethod.name().equals(method)).findFirst();
    }
}
