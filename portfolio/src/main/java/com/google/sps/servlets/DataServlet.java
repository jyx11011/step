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

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import com.google.sps.servlets.utils.UserInfoHelper;
import java.io.IOException;
import java.lang.Integer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    CommentsResult commentsResult;
    try {
      commentsResult = fetchComments(request);
    } catch(IllegalArgumentException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    Gson gson = new Gson();
    String json = gson.toJson(commentsResult);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      String urlToRedirectToAfterUserLogsIn = "/";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      response.sendRedirect(loginUrl);
      return;
    }
    
    String userId = userService.getCurrentUser().getUserId();
    String userEmail = userService.getCurrentUser().getEmail();

    String commentContent = request.getParameter("comment");
    String imageUrl = getUploadedFileUrl(request, "image-upload");
    
    Comment comment = new Comment(commentContent, userId, userEmail, imageUrl);
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
  private CommentsResult fetchComments(HttpServletRequest request) throws IllegalArgumentException {
    // Prepare datastore query
    Query query = new Query("Comment");

    // Set limit options
    FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
    Optional<Integer> limit = getLimit(request);
    if (limit.isPresent()) {
      fetchOptions = FetchOptions.Builder.withLimit(limit.get());
    }

    // Set start cursor
    Optional<Cursor> cursor = getCursor(request);
    if (cursor.isPresent()) {
      fetchOptions.startCursor(cursor.get());
    }
    
    // Set username filter
    Optional<String> username = getUsername(request);
    if (username.isPresent()) {
      query.addFilter("userEmail", FilterOperator.EQUAL, username.get());
    }

    // Add sort order
    SortOrder sortOrder = getSortOrder(request);
    query.addSort(sortOrder.property, sortOrder.sortDirection);

    // Create comment list
    PreparedQuery preparedQuery = datastore.prepare(query);

    QueryResultList<Entity> results = preparedQuery.asQueryResultList(fetchOptions);

    ArrayList<Comment> comments = new ArrayList<>();
    for (Entity entity: results) {
      Comment comment = transformEntityToComment(entity);
      comments.add(comment);
    }

    String cursorString = results.getCursor().toWebSafeString();
    if (comments.size() == 0) {
      return new CommentsResult(comments, cursorString, true);
    }
    return new CommentsResult(comments, cursorString);
  }

  /** Returns a Comment object constructed from the given entity. */
  private Comment transformEntityToComment(Entity entity) {
    String content = (String) entity.getProperty("content");
    String id = (String) entity.getProperty("id");
    String userId = (String) entity.getProperty("userId");
    String userEmail = (String) entity.getProperty("userEmail");
    String username = UserInfoHelper.getNicknameOfUser(userId).orElse(userEmail);
    long timestamp = (long) entity.getProperty("timestamp");
    String imageUrl = null;
    if (entity.getProperty("imageUrl") != null) {
      imageUrl = (String) entity.getProperty("imageUrl");
    }
    Comment comment = new Comment(id, content, userId, username, imageUrl, timestamp);
    return comment;
  }

  /** Returns a Comment entity constructed from the given Comment object. */
  private Entity transformCommentToEntity(Comment comment) {
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("content", comment.getContent());
    commentEntity.setProperty("id", comment.getIdString());
    commentEntity.setProperty("userId", comment.getUserId());
    commentEntity.setProperty("userEmail", comment.getUsername());
    commentEntity.setProperty("timestamp", comment.getTimestamp());

    if (comment.getImageUrl() != null) {
      commentEntity.setProperty("imageUrl", comment.getImageUrl());
    }
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
    switch (request.getParameter("order").toLowerCase()) {
      case "newest":
        return new SortOrder("timestamp", SortDirection.DESCENDING);
      case "oldest":
        return new SortOrder("timestamp", SortDirection.ASCENDING);
      case "user":
        return new SortOrder("userEmail", SortDirection.ASCENDING);
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

  /** Returns the starting cursor if it exists. */
  private Optional<Cursor> getCursor(HttpServletRequest request) {
    if (request.getParameter("start") == null) {
      return Optional.empty();
    }

    String startCursor = request.getParameter("start");
    Cursor cursor = Cursor.fromWebSafeString(startCursor);
    return Optional.of(cursor);
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

    SortOrder(String property, SortDirection sortDirection) {
      this.property = property;
      this.sortDirection = sortDirection;
    }
  }

  /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    BlobKey blobKey = blobKeys.get(0);

    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
    
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(options);
    }
  }

  private class CommentsResult {
    ArrayList<Comment> comments;
    String cursor;
    boolean isEndOfComments;

    CommentsResult(ArrayList<Comment> comments, String cursor, boolean isEndOfComments) {
      this.comments = comments;
      this.cursor = cursor;
      this.isEndOfComments = isEndOfComments;
    }

    CommentsResult(ArrayList<Comment> comments, String cursor) {
      this(comments, cursor, false);
    }
  }
}
