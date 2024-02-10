package com.felipe.projectmanagerapi.controllers;

import com.felipe.projectmanagerapi.dtos.*;
import com.felipe.projectmanagerapi.dtos.mappers.UserMapper;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.models.User;
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
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

  private final UserService userService;
  private final UserMapper userMapper;

  public UserController(UserService userService, UserMapper userMapper) {
    this.userService = userService;
    this.userMapper = userMapper;
  }

  @GetMapping("/auth/test")
  public String test() {
    return "Caiu aqui, CORNO";
  }

  @PostMapping("/auth/register")
  @ResponseStatus(HttpStatus.CREATED)
  public CustomResponseBody<UserResponseDTO> register(@RequestBody @Valid @NotNull UserRegisterDTO data) {
    User createdUser = this.userService.register(data);
    UserResponseDTO registerResponseDTO = this.userMapper.toDTO(createdUser);

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
    Map<String, Object> loginMap = this.userService.login(login);
    UserResponseDTO userResponseDTO = this.userMapper.toDTO((User) loginMap.get("user"));

    Map<String, Object> loginResponseMap = new HashMap<>();
    loginResponseMap.put("userInfo", userResponseDTO);
    loginResponseMap.put("token", loginMap.get("token"));

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
    List<User> users = this.userService.getAllUsers();
    List<UserResponseDTO> usersDTO = users.stream().map(this.userMapper::toDTO).toList();

    CustomResponseBody<List<UserResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os usuários");
    response.setData(usersDTO);
    return response;
  }

  @GetMapping("/users/me")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<UserResponseDTO> getAuthenticatedUserProfile() {
    User user = this.userService.getAuthenticatedUserProfile();
    UserResponseDTO userResponseDTO = this.userMapper.toDTO(user);

    CustomResponseBody<UserResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Usuário autenticado");
    response.setData(userResponseDTO);
    return response;
  }

  @GetMapping("/users/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<UserResponseDTO> getProfile(@PathVariable @NotBlank @NotNull String userId) {
    User user = this.userService.getProfile(userId);
    UserResponseDTO userResponseDTO = this.userMapper.toDTO(user);

    CustomResponseBody<UserResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Usuário encontrado");
    response.setData(userResponseDTO);
    return response;
  }

  @PatchMapping("/users/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<UserResponseDTO> updateAuthenticatedUser(
    @PathVariable @NotNull @NotBlank String userId,
    @RequestBody @Valid @NotNull UserUpdateDTO userData
  ) {
    User updatedUser = this.userService.updateAuthenticatedUser(userId, userData);
    UserResponseDTO updatedUserDTO = this.userMapper.toDTO(updatedUser);

    CustomResponseBody<UserResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Usuário atualizado com sucesso");
    response.setData(updatedUserDTO);
    return response;
  }

  @PatchMapping("/users/{userId}/role")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<UserResponseDTO> updateRole(
    @PathVariable @NotNull @NotBlank String userId,
    @RequestBody @Valid @NotNull UserRoleUpdateDTO roleData
  ) {
    User updatedUser = this.userService.updateRole(userId, roleData);
    UserResponseDTO updatedUserDTO = this.userMapper.toDTO(updatedUser);

    CustomResponseBody<UserResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Role atualizada com sucesso");
    response.setData(updatedUserDTO);
    return response;
  }

  @DeleteMapping("/users/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Map<String, UserResponseDTO>> delete(@PathVariable @NotBlank @NotNull String userId) {
    Map<String, User> deletedUser = this.userService.delete(userId);
    UserResponseDTO deletedUserDTO = this.userMapper.toDTO(deletedUser.get("deletedUser"));

    Map<String, UserResponseDTO> deletedUserResponseMap = new HashMap<>();
    deletedUserResponseMap.put("deletedUser", deletedUserDTO);

    CustomResponseBody<Map<String, UserResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Usuário deletado com sucesso");
    response.setData(deletedUserResponseMap);
    return response;
  }
}
