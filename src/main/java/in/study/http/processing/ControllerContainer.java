package in.study.http.processing;

import com.google.common.collect.Maps;
import in.study.http.request.RequestMapping;
import in.study.http.request.RequestMetadata;
import in.study.util.AnnotationScanner;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public class ControllerContainer {
    private static Map<RequestMetadata, ControllerMethod> controllerMap;
    private static ControllerMethod defaultControllerMethod = DefaultControllerMethod.getInstance();

    static {
        try {
            controllerMap = Maps.newHashMap();
            Class[] controllerClasses = AnnotationScanner.getClassesAnnotatedWith(Controller.class, "in.study");

            for (Class controllerClass : controllerClasses) {
                Method[] controllerMethods = AnnotationScanner.getMethodAnnotatedWith(controllerClass, RequestMapping.class);
                Object controller = controllerClass.newInstance();
                for (Method method : controllerMethods) {
                    RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                    System.out.println(annotation.method() + " " + annotation.path());
                    System.out.println(controllerClass.getSimpleName() + "." + method.getName());
                    controllerMap.put(
                            new RequestMetadata(annotation.method(), annotation.path()),
                            ((request, response) -> method.invoke(controller, request, response))
                    );
                }
            }
        } catch (ClassNotFoundException | IOException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Optional<ControllerMethod> findControllerMethod(RequestMetadata metadata) {
        return Optional.ofNullable(controllerMap.get(metadata));
    }

    public static ControllerMethod getControllerMethodOrDefault(RequestMetadata metadata) {
        return findControllerMethod(metadata).orElse(defaultControllerMethod);
    }
}
