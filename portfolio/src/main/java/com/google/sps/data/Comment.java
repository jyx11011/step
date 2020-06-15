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
  private String imageUrl;
  
  public Comment(String content, String userEmail) {
    this.id = UUID.randomUUID();
    this.content = content;
    this.userEmail = userEmail;
    this.timestamp = System.currentTimeMillis();
  }

  public Comment(String content, String userEmail, String imageUrl) {
    this(content, userEmail);
    this.imageUrl = imageUrl;
  }

  public Comment(String id, String content, String userEmail, long timestamp) {
    this.id = UUID.fromString(id);
    this.content = content;
    this.userEmail = userEmail;
    this.timestamp = timestamp;
  }

  public Comment(String id, String content, String userEmail, String imageUrl, long timestamp) {
    this(id, content, userEmail, timestamp);
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

  public String getUserEmail() {
    return userEmail;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
