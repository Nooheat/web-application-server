package webserver;

import com.google.common.collect.Maps;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class HttpRequest {
    private String path;
    private HttpMethod method;
    private Map<String, String> queries;
    private Map<String, String> headers;
    private Map<String, String> body;

    private HttpRequest(InputStream in) throws HttpRequestParsingException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            parseRequest(br);
        } catch (Exception e) {
            e.printStackTrace();
            throw new HttpRequestParsingException(e);
        }
    }

    private void parseRequest(BufferedReader br) throws Exception {
        parsePathAndMethod(br);
        parseHeaders(br);
        if (!(this.method == HttpMethod.GET)) {
            parseBody(br);
        }
    }

    private void parseBody(BufferedReader br) throws IOException {
        body = HttpRequestUtils.parsePayload(
                IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")))
        );
    }

    private void parsePathAndMethod(BufferedReader br) throws Exception {
        String urlAndMethodLine = br.readLine();
        String[] splited = urlAndMethodLine.split(" ", 3);

        method = HttpMethod.valueFrom(splited[0]).orElseThrow(HttpRequestParsingException::new);

        if (HttpRequestUtils.hasQueryString(splited[1])) {
            int index = splited[1].indexOf("?");
            path = splited[1].substring(0, index);
            queries = HttpRequestUtils.parsePayload(path.substring(index + 1));
            return;
        }

        path = splited[1];
        queries = Maps.newHashMap();
    }

    private void parseHeaders(BufferedReader br) throws IOException {
        headers = Maps.newHashMap();

        String line = br.readLine();

        while (line != null && !"".equals(line)) {
            Pair pair = HttpRequestUtils.parseHeader(line);
            headers.put(pair.getKey(), pair.getValue());
            line = br.readLine();
        }
    }

    public static HttpRequest of(InputStream in) throws HttpRequestParsingException {
        return new HttpRequest(in);
    }

    public String getPath() {
        return path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getQuery(String key) {
        return queries.get(key);
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public String getParam(String key) {
        return body.get(key);
    }
}
