package webserver;

import db.DataBase;
import http.*;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import template.Templater;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
            HttpResponse response = HttpResponse.of(out);


            if ("/user/create".equals(request.getPath()) && request.getMethod() == HttpMethod.POST) {
                User user = new User(request.getParam("userId"), request.getParam("password"),
                        request.getParam("name"), request.getParam("email"));
                DataBase.addUser(user);
                response.redirect("/index.html");
                return;
            }

            if ("/user/login".equals(request.getPath()) && request.getMethod() == HttpMethod.POST) {
                boolean loginSucceed = DataBase.findUserById(request.getParam("userId"))
                        .filter(user -> user.matchPassword(request.getParam("password")))
                        .isPresent();
                if (loginSucceed) {
                    response.addCookie("logined", true).redirect("/index.html");
                    return;
                }
                response.addCookie("logined", false).redirect("/index.html");
                return;
            }

            if ("/user/list".equals(request.getPath()) && request.getMethod() == HttpMethod.GET) {
                List<User> users = new ArrayList<>(DataBase.findAll());
                StringBuilder userElements = new StringBuilder();
                for (int i = 0; i < users.size(); i++) {
                    User user = users.get(i);

                    userElements.append("<tr>\n");
                    userElements.append("   <th scope=\"row\">" + (i + 1) + "</th>");
                    userElements.append("   <td>" + users.get(i).getUserId() + "</td>");
                    userElements.append("   <td>" + user.getName() + "</td>");
                    userElements.append("   <td>" + user.getEmail() + "</td>");
                    userElements.append("   <td><a href=\"#\" class=\"btn btn-success\" role=\"button\">수정</a></td>\n");
                    userElements.append("</tr>\n");
                }

                String domString = new Templater("./webapp/user/list.html")
                        .addObject("users", userElements.toString()).template();
                response.contentType(ContentType.TEXT_HTML_UTF8)
                        .body(domString)
                        .ok();
                return;
            }

            if (request.getMethod() == HttpMethod.GET && webAppFilePathes.containsKey(request.getPath())) {
                response.contentType(request.getHeader("Accept"))
                        .forward(webAppFilePathes.get(request.getPath()));
                return;
            }


            response.header(HttpHeader.CONTENT_TYPE, "text/html")
                    .body("Not Found")
                    .notFound();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }
}
