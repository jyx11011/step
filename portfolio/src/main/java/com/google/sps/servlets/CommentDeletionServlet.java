package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handle comments deletion. */
@WebServlet("/delete-comment")
public class CommentDeletionServlet extends HttpServlet {
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
     Query query = new Query("Comment");
     PreparedQuery results = datastore.prepare(query);
     
     for (Entity commentEntity: results.asIterable()) {
       datastore.delete(commentEntity.getKey());
     }
  }

}
