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
import java.lang.Integer;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.HashMap;
import java.util.TimeZone;
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
    String startingDateString = request.getParameter("start-date");
    String endingDateString = request.getParameter("end-date");
    LocalDate startingDate;
    LocalDate endingDate;
    try {
      startingDate = LocalDate.parse(startingDateString, dateFormatter);
      endingDate = LocalDate.parse(endingDateString, dateFormatter);
    } catch(DateTimeParseException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    Map<LocalDate, Integer> commentsCount = getNumberOfComments(startingDate, endingDate);
    
    Gson gson = new Gson();
    String json = gson.toJson(commentsCount);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  /** Returns a list of comment entities created between the starting date and ending date (both inclusive). */
  private QueryResultList<Entity> getComments(LocalDate startingDate, LocalDate endingDate) {
    long startOfDay = startingDate.atStartOfDay(zoneId).toEpochSecond() * 1000;
    LocalDate nextDay = endingDate.plusDays(1);
    long startOfNextDay = nextDay.atStartOfDay(zoneId).toEpochSecond() * 1000;
    
    Query query = new Query("Comment");
    query.addFilter("timestamp", FilterOperator.GREATER_THAN_OR_EQUAL, startOfDay);
    query.addFilter("timestamp", FilterOperator.LESS_THAN, startOfNextDay);
    PreparedQuery preparedQuery = datastore.prepare(query);
    QueryResultList<Entity> results = preparedQuery.asQueryResultList(FetchOptions.Builder.withDefaults());
    return results;
  }

  /** Returns the number of comments on each day in the given date range. */
  private HashMap<LocalDate, Integer> getNumberOfComments(LocalDate startingDate, LocalDate endingDate) {
    QueryResultList<Entity> comments = getComments(startingDate, endingDate);
    HashMap<LocalDate, Integer> commentsCount = new HashMap<>();
    for(Entity comment: comments) {
      LocalDate date = getDateOfTimestamp((long) comment.getProperty("timestamp"));
      if (commentsCount.containsKey(date)) {
        commentsCount.put(date, commentsCount.get(date) + 1);
      } else {
        commentsCount.put(date, 1);
      }
    }
    return commentsCount;
  }

  private LocalDate getDateOfTimestamp(long timestamp) {
    LocalDateTime dateTime = LocalDateTime.ofInstant(
      Instant.ofEpochMilli(timestamp), 
      TimeZone.getDefault().toZoneId());
    return dateTime.toLocalDate();
  }
}
