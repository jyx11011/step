package com.google.sps.servlets.utils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.util.Optional;

/**
 * Helper class for retrieving user information.
 */
public class UserInfoHelper {

  /** Returns the nickname of the user with given id if it exists. */
  public static Optional<String> getNicknameOfUser(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("User")
        .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return Optional.empty();
    }
    String nickname = (String) entity.getProperty("nickname");
    return Optional.of(nickname);
  }
}