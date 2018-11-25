package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

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
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            DataOutputStream dos = new DataOutputStream(out);

            HttpRequest request = HttpRequest.of(in);
            if ("/index.html".equals(request.getPath())) {
                response200(dos, Files.readAllBytes(new File("./webapp/index.html").toPath()));
                return;
            }

            if ("/user/form.html".equals(request.getPath())) {
                response200(dos, Files.readAllBytes(new File("./webapp/user/form.html").toPath()));
                return;
            }

            if ("/user/create".equals(request.getPath()) && request.getMethod() == HTTPMethod.POST) {
                User user = new User(request.getParam("userId"), request.getParam("password"),
                        request.getParam("name"), request.getParam("email"));
                DataBase.addUser(user);
                response200(dos, user.toString().getBytes());
                return;
            }

            byte[] body = "Hello World".getBytes();
            response200(dos, body);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    private void response200(DataOutputStream dos, byte[] body) {
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
