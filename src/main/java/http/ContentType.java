package http;

public enum ContentType {
    TEXT_HTML_UTF8("text/html; charset=utf-8"),
    APPLICATION_JSON_UTF8("application/json; charset=utf-8"),
    TEXT_CSS("text/css");

    private String contentType;

    ContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String toString() {
        return contentType;
    }
}
