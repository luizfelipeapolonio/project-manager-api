package com.felipe.projectmanagerapi.controllers;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.exceptions.InvalidDateException;
import com.felipe.projectmanagerapi.exceptions.MemberAlreadyExistsException;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.exceptions.UserAlreadyExistsException;
import com.felipe.projectmanagerapi.utils.CustomResponseBody;
import com.felipe.projectmanagerapi.utils.CustomValidationErrors;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ExceptionControllerAdvice {

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

  @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public CustomResponseBody<Void> handleAuthenticationException(Exception e) {
    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.UNAUTHORIZED);
    response.setMessage(e.getMessage());
    response.setData(null);
    return response;
  }

  @ExceptionHandler(InsufficientAuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public CustomResponseBody<Void> handleInsufficientAuthenticationException() {
    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.UNAUTHORIZED);
    response.setMessage("Autenticação é necessária para acessar este recurso");
    response.setData(null);
    return response;
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public CustomResponseBody<Void> handleAccessDeniedException(AccessDeniedException e) {
    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.FORBIDDEN);
    response.setMessage(e.getMessage().equals("Access Denied") ? "Acesso negado" : e.getMessage());
    response.setData(null);
    return response;
  }

  @ExceptionHandler(JWTVerificationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public CustomResponseBody<Void> handleJWTVerificationException(JWTVerificationException e) {
    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.UNAUTHORIZED);
    response.setMessage(e.getMessage());
    response.setData(null);
    return response;
  }

  @ExceptionHandler(JWTCreationException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public CustomResponseBody<Void> handleJWTCreationException() {
    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.INTERNAL_SERVER_ERROR);
    response.setMessage("Ocorreu um erro interno do servidor");
    response.setData(null);
    return response;
  }

  @ExceptionHandler({UserAlreadyExistsException.class, MemberAlreadyExistsException.class})
  @ResponseStatus(HttpStatus.CONFLICT)
  public CustomResponseBody<Void> handleResourceAlreadyExistsException(Exception e) {
    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.CONFLICT);
    response.setMessage(e.getMessage());
    response.setData(null);
    return response;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public CustomResponseBody<List<CustomValidationErrors>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    List<CustomValidationErrors> errors = e.getBindingResult()
      .getFieldErrors()
      .stream()
      .map(fieldError -> new CustomValidationErrors(
        fieldError.getField(),
        fieldError.getField().equalsIgnoreCase("password") ? "" : fieldError.getRejectedValue(),
        fieldError.getDefaultMessage()
      )).toList();

    CustomResponseBody<List<CustomValidationErrors>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.UNPROCESSABLE_ENTITY);
    response.setMessage("Erros de validação");
    response.setData(errors);
    return response;
  }

  @ExceptionHandler(InvalidDateException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public CustomResponseBody<Void> handleInvalidDateException(InvalidDateException e) {
    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.BAD_REQUEST);
    response.setMessage(e.getMessage());
    response.setData(null);
    return response;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public CustomResponseBody<Void> handleIllegalArgumentException(IllegalArgumentException e) {
    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.BAD_REQUEST);
    response.setMessage(e.getMessage());
    response.setData(null);
    return response;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public CustomResponseBody<List<CustomValidationErrors>> handleConstraintViolationException(ConstraintViolationException e) {
    List<CustomValidationErrors> errors = e.getConstraintViolations()
      .stream()
      .map(constraintViolation -> new CustomValidationErrors(
        constraintViolation.getPropertyPath().toString().split("\\.")[1],
        constraintViolation.getInvalidValue(),
        constraintViolation.getMessage()
      ))
      .toList();

    CustomResponseBody<List<CustomValidationErrors>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.ERROR);
    response.setCode(HttpStatus.BAD_REQUEST);
    response.setMessage("Erro ao validar parâmetros");
    response.setData(errors);
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
