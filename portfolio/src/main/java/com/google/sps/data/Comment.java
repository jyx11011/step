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
  private String imageUrl;
  
  public Comment(String content, String userId, String username) {
    this.id = UUID.randomUUID();
    this.content = content;
    this.userId = userId;
    this.username = username;
    this.timestamp = System.currentTimeMillis();
  }

  public Comment(String content, String userId, String username, String imageUrl) {
    this(content, userId, username);
    this.imageUrl = imageUrl;
  }

  public Comment(String id, String content, String userId, String username, long timestamp) {
    this.id = UUID.fromString(id);
    this.content = content;
    this.userId = userId;
    this.username = username;
    this.timestamp = timestamp;
  }

  public Comment(String id, String content, String userId, String username, String imageUrl, long timestamp) {
    this(id, content, userId, username, timestamp);
    this.imageUrl = imageUrl;
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

  public String getImageUrl() {
    return imageUrl;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
