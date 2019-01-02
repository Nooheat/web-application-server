package in.study.controller;

import in.study.http.processing.Controller;
import in.study.http.request.HttpMethod;
import in.study.http.request.HttpRequest;
import in.study.http.response.HttpResponse;
import in.study.http.request.RequestMapping;

@Controller
public class HelloController {

    @RequestMapping(path = "/hello", method = HttpMethod.GET)
    public void hello(HttpRequest request, HttpResponse response) {
        response.body("I gotcha!! your user-agent is : " + request.getHeader("User-Agent"))
                .ok();
    }
}
