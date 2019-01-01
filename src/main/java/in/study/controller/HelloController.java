package in.study.controller;

import in.study.http.HttpMethod;
import in.study.http.HttpRequest;
import in.study.http.HttpResponse;
import in.study.util.RequestMapping;

@Controller
public class HelloController {

    @RequestMapping(path = "/hello", method = HttpMethod.GET)
    public void hello(HttpRequest request, HttpResponse response) {
        response.body("I gotcha!! your user-agent is : " + request.getHeader("User-Agent"))
                .ok();
    }
}
