package in.study.http;

public enum HttpStatus {
    OK(200, "200 OK"),
    FOUND(302, "302 Found"),
    BAD_REQUEST(400, "400 BadRequest"),
    NOT_FOUND(404, "404 NotFound");

    private int statusCode;
    private String description;

    HttpStatus(int statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
