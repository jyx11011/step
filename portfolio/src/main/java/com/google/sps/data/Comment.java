package com.google.sps.data;

import java.util.UUID;

/**
 * Class representing the comment.
 */
public class Comment {
  private UUID id;
  private String content;
  private String userId;
  private String username;
  private long timestamp;
  
  public Comment(String content, String userId, String username) {
    this.id = UUID.randomUUID();
    this.content = content;
    this.userId = userId;
    this.username = username;
    this.timestamp = System.currentTimeMillis();
  }

  public Comment(String id, String content, String userId, String username, long timestamp) {
    this.id = UUID.fromString(id);
    this.content = content;
    this.userId = userId;
    this.username = username;
    this.timestamp = timestamp;
  }

  public UUID getId() {
    return id;
  }

  public String getIdString() {
    return id.toString();
  }

  public String getContent() {
    return content;
  }

  public String getUserId() {
    return userId;
  }

  public String getUsername() {
    return username;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
