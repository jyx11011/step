package com.google.sps.data;

import java.util.UUID;

/**
 * Class representing the comment.
 */
public class Comment {
  private UUID id;
  private String content;
  private String userEmail;
  private long timestamp;
  
  public Comment(String content, String userEmail) {
    this.id = UUID.randomUUID();
    this.content = content;
    this.userEmail = userEmail;
    this.timestamp = System.currentTimeMillis();
  }

  public Comment(String id, String content, String userEmail, long timestamp) {
    this.id = UUID.fromString(id);
    this.content = content;
    this.userEmail = userEmail;
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

  public String getUserEmail() {
    return userEmail;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
