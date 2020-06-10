package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles comments statistic request. */
@WebServlet("/comments-stats")
public class CommentsStatisticServlet extends HttpServlet {
  
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
  private ZoneId zoneId = ZoneId.systemDefault();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String dateString = request.getParameter("date");
    LocalDate date;
    try {
      date = LocalDate.parse(dateString, dateFormatter);
    } catch(DateTimeParseException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    int count = getNumberOfCommentsOnDate(date);
    response.setContentType("text/html;");
    response.getWriter().println(count);
  }

  /** Returns the number comments on the given date. */
  private int getNumberOfCommentsOnDate(LocalDate date) {
    long startOfDay = date.atStartOfDay(zoneId).toEpochSecond() * 1000;
    LocalDate nextDay = date.plusDays(1);
    long startOfNextDay = nextDay.atStartOfDay(zoneId).toEpochSecond() * 1000;

    Query query = new Query("Comment");
    query.addFilter("timestamp", FilterOperator.GREATER_THAN_OR_EQUAL, startOfDay);
    query.addFilter("timestamp", FilterOperator.LESS_THAN, startOfNextDay);

    PreparedQuery results = datastore.prepare(query);
    int count = results.countEntities(FetchOptions.Builder.withDefaults());
    return count;
  }
}
