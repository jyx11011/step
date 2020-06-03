package com.google.sps.data;

import java.util.UUID;

/**
 * Class representing the comment.
 */
public class Comment {
  private UUID id;
  private String content;
  
  public Comment(String content) {
    this.id = UUID.randomUUID();
    this.content = content;
  }

  public Comment(String id, String content) {
    this.id = UUID.fromString(id);
    this.content = content;
  }

  public UUID getId() {
    return id;
  }

  public String getContent() {
    return content;
  }
}