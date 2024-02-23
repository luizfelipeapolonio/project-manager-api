package com.felipe.projectmanagerapi.exceptions;

public class InvalidDateException extends RuntimeException {
  public InvalidDateException(String message) {
    super(message);
  }
}
