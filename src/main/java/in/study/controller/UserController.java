package in.study.controller;

import in.study.db.DataBase;
import in.study.http.ContentType;
import in.study.http.HttpMethod;
import in.study.http.HttpRequest;
import in.study.http.HttpResponse;
import in.study.model.User;
import in.study.template.Templater;
import in.study.util.RequestMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UserController {

    @RequestMapping(path = "/user/create", method = HttpMethod.POST)
    public void signup(HttpRequest request, HttpResponse response) {
        User user = new User(request.getParam("userId"), request.getParam("password"),
                request.getParam("name"), request.getParam("email"));
        DataBase.addUser(user);
        response.redirect("/index.html");
    }

    @RequestMapping(path = "/user/list", method = HttpMethod.GET)
    public void userList(HttpRequest request, HttpResponse response) throws IOException {
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
    }
}
