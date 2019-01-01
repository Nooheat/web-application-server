package in.study.controller;

import in.study.http.HttpRequest;
import in.study.http.HttpResponse;

import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface ControllerMethod {

    void run(HttpRequest request, HttpResponse response) throws InvocationTargetException, IllegalAccessException;
}
