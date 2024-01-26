package com.felipe.projectmanagerapi.controllers;

import com.felipe.projectmanagerapi.dtos.*;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.services.UserService;
import com.felipe.projectmanagerapi.utils.CustomResponseBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/auth/test")
  public String test() {
    return "Caiu aqui, CORNO";
  }

  @PostMapping("/auth/register")
  @ResponseStatus(HttpStatus.CREATED)
  public CustomResponseBody<UserResponseDTO> register(@RequestBody @Valid @NotNull UserRegisterDTO data) {
    UserResponseDTO registerResponseDTO = this.userService.register(data);

    CustomResponseBody<UserResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.CREATED);
    response.setMessage("Usuário criado com sucesso");
    response.setData(registerResponseDTO);
    return response;
  }

  @PostMapping("/auth/login")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Map<String, Object>> login(@RequestBody @Valid @NotNull LoginDTO login) {
    Map<String, Object> loginResponseMap = this.userService.login(login);

    CustomResponseBody<Map<String, Object>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Usuário logado");
    response.setData(loginResponseMap);
    return response;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<List<UserResponseDTO>> getAllUsers() {
    List<UserResponseDTO> users = this.userService.getAllUsers();

    CustomResponseBody<List<UserResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os usuários");
    response.setData(users);
    return response;
  }

  @GetMapping("/users/profile")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<UserResponseDTO> getAuthenticatedUserProfile() {
    UserResponseDTO user = this.userService.getAuthenticatedUserProfile();

    CustomResponseBody<UserResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Usuário autenticado");
    response.setData(user);
    return response;
  }

  @PatchMapping("/users/{userId}/profile")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<UserResponseDTO> updateAuthenticatedUser(
    @PathVariable @NotNull @NotBlank String userId,
    @RequestBody @Valid @NotNull UserUpdateDTO userData
  ) {
    UserResponseDTO updatedUser = this.userService.updateAuthenticatedUser(userId, userData);

    CustomResponseBody<UserResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Usuário atualizado com sucesso");
    response.setData(updatedUser);
    return response;
  }

  @PatchMapping("/users/{userId}/role")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<UserResponseDTO> updateRole(
    @PathVariable @NotNull @NotBlank String userId,
    @RequestBody @Valid @NotNull UserRoleUpdateDTO roleData
  ) {
    UserResponseDTO updatedUser = this.userService.updateRole(userId, roleData);

    CustomResponseBody<UserResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Role atualizada com sucesso");
    response.setData(updatedUser);
    return response;
  }
}
