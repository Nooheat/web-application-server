package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static Map<String, Path> webAppFilePathes;
    private static final String WEBAPP_ROOT = "./webapp";

    static {
        try {
            webAppFilePathes = Files.find(Paths.get(WEBAPP_ROOT),
                    Integer.MAX_VALUE,
                    (filePath, fileAttr) -> fileAttr.isRegularFile())
                    .collect(toMap(
                            path -> path.toString().substring(WEBAPP_ROOT.length()),
                            path -> path));
        } catch (IOException e) {
            e.printStackTrace();
            log.debug("IOException Occurred");
        }
    }

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


            if ("/user/create".equals(request.getPath()) && request.getMethod() == HttpMethod.POST) {
                User user = new User(request.getParam("userId"), request.getParam("password"),
                        request.getParam("name"), request.getParam("email"));
                DataBase.addUser(user);
                HttpResponse response = HttpResponse.redirect("/index.html").build();
                dos.writeBytes(response.toString());
                return;
            }

            if ("/user/login".equals(request.getPath()) && request.getMethod() == HttpMethod.POST) {
                boolean loginSucceed = DataBase.findUserById(request.getParam("userId"))
                        .filter(user -> user.matchPassword(request.getParam("password")))
                        .isPresent();
                if (loginSucceed) {
                    HttpResponse response = HttpResponse.redirect("/index.html")
                            .addCookie("logined", true)
                            .build();
                    dos.writeBytes(response.toString());
                    return;
                }

                HttpResponse response = HttpResponse.redirect("/index.html")
                        .addCookie("logined", false)
                        .build();
                dos.writeBytes(response.toString());
                return;
            }

            if (request.getMethod() == HttpMethod.GET && webAppFilePathes.containsKey(request.getPath())) {
                byte[] content = Files.readAllBytes(webAppFilePathes.get(request.getPath()));
                response200(dos, content, request.getHeader("Accept"));
                return;
            }


            HttpResponse response = HttpResponse.notFound()
                    .header(HttpHeader.CONTENT_TYPE, "text/html")
                    .body("Not Found")
                    .build();
            dos.writeBytes(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    private void response(DataOutputStream dos, HttpStatus status, byte[] body, Pair... headers) {
        try {
            dos.writeBytes("HTTP/1.1 " + status + "\r\n");
            for (Pair header : headers) {
                dos.writeBytes(header.getKey() + ": " + header.getValue() + "\r\n");
            }
            dos.writeBytes("\r\n");
            if (body != null) {
                dos.write(body, 0, body.length);
            }
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200(DataOutputStream dos, byte[] body, String contentType) {
        response(
                dos,
                HttpStatus.OK,
                body,
                Pair.of("Content-Type", contentType),
                Pair.of("Content-Length", Integer.toString(body.length))
        );
    }
}
