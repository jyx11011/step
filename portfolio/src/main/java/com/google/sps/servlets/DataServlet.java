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
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
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
    ArrayList<Comment> comments = fetchComments(request);
    Gson gson = new Gson();
    String json = gson.toJson(comments);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String user = request.getParameter("username");
    String commentContent = request.getParameter("comment");
    
    Comment comment = new Comment(commentContent, user);
    Entity commentEntity = transformCommentToEntity(comment);
    datastore.put(commentEntity);

    response.sendRedirect("/index.html");
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (request.getParameterMap().containsKey("id")) {
      String commentId = request.getParameter("id");
      deleteComment(commentId);
    } else {
      deleteAllComments();
    }
  }

  /** Returns comments fetched from datastore */
  private ArrayList<Comment> fetchComments(HttpServletRequest request) {
    // Prepare datastore query
    Query query = new Query("Comment");

    // Set limit options
    FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
    Optional<Integer> limit = getLimit(request);
    if (limit.isPresent()) {
      fetchOptions = FetchOptions.Builder.withLimit(limit.get());
    }
    
    // Set username filter
    Optional<String> username = getUsername(request);
    if (username.isPresent()) {
      query.addFilter("user", FilterOperator.EQUAL, username.get());
    }

    // Add sort order
    SortOrder sortOrder = getSortOrder(request);
    query.addSort(sortOrder.property, sortOrder.sortDirection);

    // Create comment list

    PreparedQuery results = datastore.prepare(query);

    ArrayList<Comment> comments = new ArrayList<>();
    for (Entity entity: results.asIterable(fetchOptions)) {
      Comment comment = transformEntityToComment(entity);
      comments.add(comment);
    }
    return comments;
  }

  /** Returns a Comment object constructed from the given entity. */
  private Comment transformEntityToComment(Entity entity) {
    String content = (String) entity.getProperty("content");
    String id = (String) entity.getProperty("id");
    String user = (String) entity.getProperty("user");
    long timestamp = (long) entity.getProperty("timestamp");
    Comment comment = new Comment(id, content, user, timestamp);
    return comment;
  }

  /** Returns a Comment entity constructed from the given Comment object. */
  private Entity transformCommentToEntity(Comment comment) {
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("content", comment.getContent());
    commentEntity.setProperty("id", comment.getIdString());
    commentEntity.setProperty("user", comment.getUser());
    commentEntity.setProperty("timestamp", comment.getTimestamp());
    return commentEntity;
  }

  /** Returns the username requested by the client if it exists. */
  private Optional<String> getUsername(HttpServletRequest request) {
    if (request.getParameter("username") == null) {
      return Optional.empty();
    }
    return Optional.of(request.getParameter("username"));
  }

  /** 
   * Returns the order for sorting comments requested by the client if it exists.
   * Otherwise, returns sorting by newest by default.
   */
  private SortOrder getSortOrder(HttpServletRequest request) {
    if (request.getParameter("order") == null) {
      return new SortOrder("timestamp", SortDirection.DESCENDING);
    }
    switch (request.getParameter("order")) {
      case "Newest":
        return new SortOrder("timestamp", SortDirection.DESCENDING);
      case "Oldest":
        return new SortOrder("timestamp", SortDirection.ASCENDING);
      case "User":
        return new SortOrder("user", SortDirection.ASCENDING);
      default:
        return new SortOrder("timestamp", SortDirection.DESCENDING);
    }
  }

  /** Returns the limit of comments if the limit a non-negative interger. Otherwise, returns empty. */
  private Optional<Integer> getLimit(HttpServletRequest request) {
    if (request.getParameter("limit") == null) {
      return Optional.empty();
    }

    // Check that the string is a valid integer.
    String limitString = request.getParameter("limit");
    int limit = -1;
    try {
      limit = Integer.parseInt(limitString);
    } catch (NumberFormatException e) {
      return Optional.empty();
    }

    // Check that the number is non-negative
    if (limit < 0) {
      return Optional.empty();
    }
    return Optional.of(limit);
  }

  /** Deletes all comments in datastore. */
  private void deleteAllComments() {
    Query query = new Query("Comment");
    PreparedQuery results = datastore.prepare(query);
    
    for (Entity commentEntity: results.asIterable()) {
      datastore.delete(commentEntity.getKey());
    }
  }

  /** Deletes the comment with the given id. */
  private void deleteComment(String id) {
    Query query = new Query("Comment").addFilter("id", FilterOperator.EQUAL, id);
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity: results.asIterable()) {
      datastore.delete(entity.getKey());
    }
  }

  private class SortOrder {
    String property;
    SortDirection sortDirection;

    public SortOrder(String property, SortDirection sortDirection) {
      this.property = property;
      this.sortDirection = sortDirection;
    }
  }
}
