package com.felipe.projectmanagerapi.enums;

public enum Role {
  ADMIN("ADMIN", 1),
  WRITE_READ("WRITE_READ", 2),
  READ_ONLY("READ_ONLY", 3);

  private final String name;
  private final int code;

  Role(String name, int code) {
    this.name = name;
    this.code = code;
  }

  public String getName() {
    return this.name;
  }

  public int getCode() {
    return this.code;
  }
}
