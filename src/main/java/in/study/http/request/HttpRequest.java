package in.study.http.request;

import com.google.common.collect.Maps;
import in.study.http.HttpHeader;
import in.study.http.session.HttpSession;
import in.study.http.session.HttpSessions;
import in.study.util.HttpRequestUtils;
import in.study.util.IOUtils;
import in.study.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class HttpRequest {
    private Map<String, String> queries;
    private Map<String, String> cookies;
    private Map<String, String> headers;
    private Map<String, String> body;
    private RequestMetadata metadata;
    private HttpSession session;

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
        parseCookies();
        getSession();
        if (!(this.metadata.getMethod() == HttpMethod.GET)) {
            parseBody(br);
        }
    }

    private void getSession() {
        this.session = HttpSessions.getSession(cookies.get("JSESSIONID"));
    }

    private void parseCookies() {
        this.cookies = HttpRequestUtils.parseCookies(getHeader(HttpHeader.COOKIE));
    }

    private void parseBody(BufferedReader br) throws IOException {
        body = HttpRequestUtils.parsePayload(
                IOUtils.readData(br, Integer.parseInt(getHeader(HttpHeader.CONTENT_LENGTH)))
        );
    }

    private void parsePathAndMethod(BufferedReader br) throws Exception {
        String urlAndMethodLine = br.readLine();
        String[] splited = urlAndMethodLine.split(" ", 3);

        HttpMethod method = HttpMethod.valueFrom(splited[0]).orElseThrow(HttpRequestParsingException::new);

        if (HttpRequestUtils.hasQueryString(splited[1])) {
            int index = splited[1].indexOf("?");
            String path = splited[1].substring(0, index);
            this.metadata = new RequestMetadata(method, path);
            queries = HttpRequestUtils.parsePayload(path.substring(index + 1));
            return;
        }

        String path = splited[1];
        this.metadata = new RequestMetadata(method, path);
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
        return metadata.getPath();
    }

    public HttpMethod getMethod() {
        return metadata.getMethod();
    }

    public String getQuery(String key) {
        return queries.get(key);
    }

    public String getHeader(HttpHeader header) {
        return getHeader(header.toString());
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public String getParam(String key) {
        return body.get(key);
    }

    public RequestMetadata getMetadata() {
        return metadata;
    }
}
