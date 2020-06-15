package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.servlets.utils.UserInfoHelper;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/user")
public class UserServlet extends HttpServlet {

  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    JsonObject json = new JsonObject();
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterUserLogsOut = "/";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
      
      String userId = userService.getCurrentUser().getUserId();
      Optional<String> nickname = UserInfoHelper.getNicknameOfUser(userEmail);
      if (nickname.isPresent()) {
        json.addProperty("nickname", nickname.get());
      }

      json.addProperty("isLoggedIn", true);
      json.addProperty("userEmail", userEmail);
      json.addProperty("logoutUrl", logoutUrl);
    } else {
      String urlToRedirectToAfterUserLogsIn = "/";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);

      json.addProperty("isLoggedIn", false);
      json.addProperty("loginUrl", loginUrl);
    }
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String userNickname = request.getParameter("nickname");
      String id = userService.getCurrentUser().getUserId();

      Entity entity = new Entity("User", id);
      entity.setProperty("id", id);
      entity.setProperty("nickname", userNickname);
      entity.setProperty("email", userEmail);
      datastore.put(entity);
      response.sendRedirect("/index.html");
    } else {
      String urlToRedirectToAfterUserLogsIn = "/";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      response.sendRedirect(loginUrl);
    }
  }
}
