package webserver;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.stream.Collectors;

public class HttpResponse<T> {
    private HttpStatus status;
    private Map<String, String> headers;
    private T body;


    private HttpResponse(HttpStatus status, Map<String, String> headers, T body) {
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    public static <T> HttpResponseBuilder<T> ok() {
        return new HttpResponseBuilder<T>(HttpStatus.OK);
    }

    public static <T> HttpResponseBuilder<T> notFound() {
        return new HttpResponseBuilder<T>(HttpStatus.NOT_FOUND);
    }

    public static <T> HttpResponseBuilder<T> redirect(String location) {
        return new HttpResponseBuilder<T>(HttpStatus.FOUND)
                .header("Location", location);
    }

    public static <T> HttpResponseBuilder<T> builder(HttpStatus status) {
        return new HttpResponseBuilder<>(status);
    }

    @Override
    public String toString() {
        return ("HTTP/1.1 " + status + "\r\n") +
                headers.entrySet().stream()
                        .map(pair -> String.format("%s: %s", pair.getKey(), pair.getValue()))
                        .collect(Collectors.joining("\r\n", "", "\r\n")) +
                "\r\n" +
                body;
    }

    public static class HttpResponseBuilder<T> {
        private HttpStatus status;
        private Map<String, String> headers = Maps.newHashMap();
        private T body;


        HttpResponseBuilder(HttpStatus status) {
            this.status = status;
        }

        public HttpResponseBuilder<T> addCookie(String key, Object value) {
            headers.merge(
                    "Set-Cookie",
                    key + "=" + value.toString(),
                    (existingValue, newValue) -> existingValue + ";" + newValue
            );
            return this;
        }

        public HttpResponseBuilder<T> header(String key, Object value) {
            this.headers.put(key, value.toString());
            return this;
        }

        public HttpResponseBuilder<T> header(HttpHeader header, Object value) {
            this.headers.put(header.toString(), value.toString());
            return this;
        }

        public HttpResponseBuilder<T> body(T body) {
            this.body = body;
            if (body == null) {
                return header(HttpHeader.CONTENT_LENGTH, 0);
            }
            return this;
        }

        public HttpResponse<T> build() {
            return new HttpResponse<>(status, headers, body);
        }
    }
}
