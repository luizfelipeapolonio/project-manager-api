package com.felipe.projectmanagerapi.exceptions;

public class InvalidBudgetException extends RuntimeException {
  public InvalidBudgetException(String message) {
    super(message);
  }
}
