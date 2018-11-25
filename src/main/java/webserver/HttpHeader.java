package webserver;

public enum HttpHeader {
    CONTENT_TYPE("Content-Type"),
    LOCATION("Location"),
    CONTENT_LENGTH("Content-Length"),
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
