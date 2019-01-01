package in.study.webserver;

import in.study.controller.ControllerMethod;
import in.study.db.DataBase;
import in.study.http.HttpHeader;
import in.study.http.HttpMethod;
import in.study.http.HttpRequest;
import in.study.http.HttpResponse;
import in.study.util.RequestMetadata;
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
import java.util.Optional;

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

            RequestMetadata metadata = request.getMetadata();

            Optional<ControllerMethod> controllerMethod = ControllerContainer.findControllerMethod(metadata);
            if (controllerMethod.isPresent()) {
                controllerMethod.get().run(request, response);
                return;
            }

//            if ("/user/create".equals(request.getPath()) && request.getMethod() == HttpMethod.POST) {
//                User user = new User(request.getParam("userId"), request.getParam("password"),
//                        request.getParam("name"), request.getParam("email"));
//                DataBase.addUser(user);
//                response.redirect("/index.html");
//                return;
//            }

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

            // TODO controllerContainer로!!
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
