package in.study.webserver;

import in.study.http.processing.ControllerContainer;
import in.study.http.request.HttpRequest;
import in.study.http.request.RequestMetadata;
import in.study.http.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = HttpRequest.of(in);
            HttpResponse response = HttpResponse.of(out);

            RequestMetadata metadata = request.getMetadata();

            ControllerContainer
                    .getControllerMethodOrDefault(metadata)
                    .run(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }
}
