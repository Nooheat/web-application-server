package in.study.http.processing;

import in.study.http.request.HttpRequest;
import in.study.http.response.HttpResponse;

import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface ControllerMethod {

    void run(HttpRequest request, HttpResponse response) throws InvocationTargetException, IllegalAccessException;
}
