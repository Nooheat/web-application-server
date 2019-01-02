package in.study.http.processing;

import in.study.http.HttpHeader;
import in.study.http.request.HttpMethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

class DefaultControllerMethod {
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
        }
    }

    private static final ControllerMethod defaultControllerMethod = ((request, response) -> {
        if (request.getMethod() == HttpMethod.GET && webAppFilePathes.containsKey(request.getPath())) {
            response.contentType(request.getHeader("Accept")).forward(webAppFilePathes.get(request.getPath()));
            return;
        }

        response.header(HttpHeader.CONTENT_TYPE, "text/html")
                .body("Not Found")
                .notFound();
    });

    public static ControllerMethod getInstance() {
        return defaultControllerMethod;
    }
}
