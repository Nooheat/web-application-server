package in.study.http.response;

import com.google.common.collect.Maps;
import in.study.http.ContentType;
import in.study.http.HttpHeader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class HttpResponse<T> {
    private DataOutputStream dos;
    private HttpStatus status;
    private Map<String, String> headers;
    private T body;

    private HttpResponse(OutputStream os) {
        this.dos = new DataOutputStream(os);
        this.headers = Maps.newHashMap();
    }

    public HttpResponse<T> body(T body) {
        if (body != null) {
            header(HttpHeader.CONTENT_LENGTH, body.toString().length());
        }
        this.body = body;
        return this;
    }

    public HttpResponse<T> addCookie(String key, Object value) {
        this.headers.merge(
                "Set-Cookie",
                key + "=" + value.toString(),
                (existingValue, newValue) -> existingValue + ";" + newValue
        );
        return this;
    }

    public HttpResponse<T> header(HttpHeader header, Object value) {
        this.headers.put(header.toString(), value.toString());
        return this;
    }

    public HttpResponse<T> contentType(ContentType contentType) {
        return contentType(contentType.toString());
    }

    public HttpResponse<T> contentType(String contentType) {
        return header(HttpHeader.CONTENT_TYPE, contentType);
    }

    public void ok() {
        this.status = HttpStatus.OK;
        done();
    }

    public void notFound() {
        this.status = HttpStatus.NOT_FOUND;
        done();
    }

    public void redirect(String location) {
        this.status = HttpStatus.FOUND;
        header(HttpHeader.LOCATION, location);
        done();
    }

    public void forward(Path path) {
        try {
            byte[] fileContents = Files.readAllBytes(path);
            header(HttpHeader.CONTENT_LENGTH, fileContents.length);
            writeStatus();
            writeHeaders();
            dos.writeBytes("\r\n");
            dos.write(fileContents, 0, fileContents.length);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void done() {
        try {
            writeStatus();
            writeHeaders();
            writeBody();
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeStatus() throws IOException {
        dos.writeBytes("HTTP/1.1 " + status + "\r\n");
    }

    private void writeHeaders() throws IOException {
        if (body != null) {
            header(HttpHeader.CONTENT_LENGTH, body.toString().length());
        }
        for (Map.Entry<String, String> header : headers.entrySet()) {
            dos.writeBytes(header.getKey() + ": " + header.getValue() + "\r\n");
        }
    }

    private void writeBody() throws IOException {
        dos.writeBytes("\r\n");
        if (body != null) {
            dos.writeBytes(body.toString());
        }
        dos.flush();
    }


    public static HttpResponse of(OutputStream os) {
        return new HttpResponse(os);
    }
}
