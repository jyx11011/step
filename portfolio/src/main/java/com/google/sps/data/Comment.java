package com.google.sps.data;

import java.util.UUID;

/**
 * Class representing the comment.
 */
public class Comment {
  private UUID id;
  private String content;
  private String user;
  
  public Comment(String content, String user) {
    this.id = UUID.randomUUID();
    this.content = content;
    this.user = user;
  }

  public Comment(String id, String content, String user) {
    this.id = UUID.fromString(id);
    this.content = content;
    this.user = user;
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
}
