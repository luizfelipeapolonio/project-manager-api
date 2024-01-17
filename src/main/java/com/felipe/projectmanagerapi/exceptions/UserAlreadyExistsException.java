package com.felipe.projectmanagerapi.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException() {
    super("Usuário já cadastrado");
  }
}
