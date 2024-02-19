package com.felipe.projectmanagerapi.enums;

public enum PriorityLevel {
  HIGH("alta", 1),
  MEDIUM("media", 2),
  LOW("baixa", 3);

  private final String value;
  private final int level;

  PriorityLevel(String value, int level) {
    this.value = value;
    this.level = level;
  }

  public String getValue() {
    return this.value;
  }

  public int getLevel() {
    return this.level;
  }
}
