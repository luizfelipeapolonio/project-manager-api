package com.felipe.projectmanagerapi.controllers;

import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.exceptions.UserAlreadyExistsException;
import com.felipe.projectmanagerapi.utils.CustomResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApplicationControllerAdvice {

  @ExceptionHandler(RecordNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public CustomResponseBody<Void> handleRecordNotFoundException(RecordNotFoundException e) {
    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.NOT_FOUND);
    response.setMessage(e.getMessage());
    response.setData(null);
    return response;
  }


  @ExceptionHandler(UserAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public CustomResponseBody<Void> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.CONFLICT);
    response.setMessage(e.getMessage());
    response.setData(null);
    return response;
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public CustomResponseBody<Void> handleUncaughtException() {
    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.INTERNAL_SERVER_ERROR);
    response.setMessage("Ocorreu um erro interno do servidor");
    response.setData(null);
    return response;
  }
}
