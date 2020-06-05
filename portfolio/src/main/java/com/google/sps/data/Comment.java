package com.google.sps.data;

import java.util.UUID;

/**
 * Class representing the comment.
 */
public class Comment {
  private UUID id;
  private String content;
  private String user;
  private long timestamp;
  
  public Comment(String content, String user) {
    this.id = UUID.randomUUID();
    this.content = content;
    this.user = user;
    this.timestamp = System.currentTimeMillis();
  }

  public Comment(String id, String content, String user, long timestamp) {
    this.id = UUID.fromString(id);
    this.content = content;
    this.user = user;
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

  public String getUser() {
    return user;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
