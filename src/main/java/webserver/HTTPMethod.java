package webserver;

import java.util.Arrays;
import java.util.Optional;

public enum HTTPMethod {
    POST,
    GET,
    PUT,
    DELETE,
    PATCH;

    public static Optional<HTTPMethod> valueFrom(String method) {
        return Arrays.stream(values()).filter(httpMethod -> httpMethod.name().equals(method)).findFirst();
    }
}
