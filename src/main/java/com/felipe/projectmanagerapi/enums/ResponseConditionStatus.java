package com.felipe.projectmanagerapi.enums;

public enum ResponseConditionStatus {
  SUCCESS("Success"),
  ERROR("Error");

  private final String value;

  ResponseConditionStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
