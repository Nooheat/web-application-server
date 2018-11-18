package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

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

            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String urlLine = br.readLine();
            String[] splited = urlLine.split(" ", 3);

            if (splited.length != 3) {
                response400Header(dos);
                return;
            }

            String url = splited[1];

            if ("/index.html".equals(HttpRequestUtils.parsePlainUrl(url))) {
                response200(dos, Files.readAllBytes(new File("./webapp/index.html").toPath()));
                return;
            }

            if ("/user/form.html".equals(HttpRequestUtils.parsePlainUrl(url))) {
                response200(dos, Files.readAllBytes(new File("./webapp/user/form.html").toPath()));
                return;
            }

            if ("/user/create".equals(HttpRequestUtils.parsePlainUrl(url))) {
                Map<String, String> queryMap = HttpRequestUtils.parseQueryStringFromUrl(url);
                User user = new User(queryMap.get("userId"), queryMap.get("password"),
                        queryMap.get("name"), queryMap.get("email"));
                DataBase.addUser(user);
                response200(dos, user.toString().getBytes());
            }
            byte[] body = "Hello World".getBytes();
            response200(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200(DataOutputStream dos, byte[] body) {
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void response400Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 400 BadRequest \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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
