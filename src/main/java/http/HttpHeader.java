package http;

public enum HttpHeader {
    CONTENT_TYPE("Content-Type"),
    LOCATION("Location"),
    CONTENT_LENGTH("Content-Length"),
    COOKIE("Cookie"),
    SET_COOKIE("Set-Cookie"),
    ACCEPT("Accept");

    private String header;

    HttpHeader(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}
