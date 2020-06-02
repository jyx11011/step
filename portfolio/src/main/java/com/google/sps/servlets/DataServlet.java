// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import java.io.IOException;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handle comments data */
@WebServlet("/comments")
public class DataServlet extends HttpServlet {
  
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ArrayList<String> comments = fetchComments(request);
    Gson gson = new Gson();
    String json = gson.toJson(comments);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = request.getParameter("comment");

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("content", comment);
    datastore.put(commentEntity);

    response.sendRedirect("/index.html");
  }

  /** Returns comments fetched from datastore */
  private ArrayList<String> fetchComments(HttpServletRequest request) {
    //Prepare datastore query
    Query query = new Query("Comment");
    PreparedQuery results = datastore.prepare(query);
    
    //Set limit options
    FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
    Optional<Integer> limit = getLimit(request);
    if (limit.isPresent()) {
      fetchOptions = FetchOptions.Builder.withLimit(limit.get());
    }

    //Create comment list
    ArrayList<String> comments = new ArrayList<>();
    for (Entity entity: results.asIterable(fetchOptions)) {
      String content = (String) entity.getProperty("content");
      comments.add(content);
    }
    return comments;
  }

  /** Returns the limit of comments if the limit a non-negative interger. Otherwise, returns empty. */
  private Optional<Integer> getLimit(HttpServletRequest request) {
    if (request.getParameter("limit") == null) {
      return Optional.empty();
    }

    //Check that the string is a valid integer.
    String limitString = request.getParameter("limit");
    int limit = -1;
    try {
      limit = Integer.parseInt(limitString);
    } catch (NumberFormatException e) {
      return Optional.empty();
    }

    //Check that the number is non-negative
    if (limit < 0) {
      return Optional.empty();
    }
    return Optional.of(limit);
  }
}
