package com.felipe.projectmanagerapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipe.projectmanagerapi.dtos.*;
import com.felipe.projectmanagerapi.dtos.mappers.UserMapper;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.exceptions.UserAlreadyExistsException;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.services.UserService;
import com.felipe.projectmanagerapi.utils.CustomResponseBody;
import com.felipe.projectmanagerapi.utils.GenerateMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "test")
public class UserControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  UserService userService;

  @Spy
  UserMapper userMapper;

  private AutoCloseable closeable;
  private String baseUrl;
  private LocalDateTime mockDateTime;
  private GenerateMocks dataMock;

  @BeforeEach
  void setUp() {
    this.closeable = MockitoAnnotations.openMocks(this);
    this.baseUrl = "/api";
    this.mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");
    this.dataMock = new GenerateMocks();
  }

  @AfterEach
  void tearDown() throws Exception {
    this.closeable.close();
  }

  @Test
  @DisplayName("register - Should successfully register a user and return a CustomResponseBody with user's info")
  void userRegisterSuccess() throws Exception {
    UserRegisterDTO data = new UserRegisterDTO(
      "User 1",
      "teste1@email.com",
      "123456",
      "WRITE_READ"
    );
    User user = new User();
    user.setId("01");
    user.setName(data.name());
    user.setEmail(data.email());
    user.setPassword(data.password());
    user.setRole(this.dataMock.getUsers().get(1).getRole());
    user.setCreatedAt(this.mockDateTime);
    user.setUpdatedAt(this.mockDateTime);

    UserResponseDTO createdUserDTO = new UserResponseDTO(
      "01",
      data.name(),
      data.email(),
      data.role(),
      this.mockDateTime,
      this.mockDateTime
    );
    String jsonBody = this.objectMapper.writeValueAsString(data);

    when(this.userService.register(data)).thenReturn(user);

    this.mockMvc.perform(post(this.baseUrl + "/auth/register")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.CREATED.value()))
      .andExpect(jsonPath("$.message").value("Usuário criado com sucesso"))
      .andExpect(jsonPath("$.data.id").value(createdUserDTO.id()))
      .andExpect(jsonPath("$.data.name").value(createdUserDTO.name()))
      .andExpect(jsonPath("$.data.email").value(createdUserDTO.email()))
      .andExpect(jsonPath("$.data.role").value(createdUserDTO.role()))
      .andExpect(jsonPath("$.data.createdAt").value(createdUserDTO.createdAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(createdUserDTO.updatedAt().toString()));

    verify(this.userService, times(1)).register(data);
  }

  @Test
  @DisplayName("register - Should return an error response with a conflict status code if user already exists")
  void userRegisterFailsByExistingUser() throws Exception {
    UserRegisterDTO registerDTO = new UserRegisterDTO("User 1", "teste1@email.com", "123456", "WRITE_READ");
    String jsonBody = this.objectMapper.writeValueAsString(registerDTO);

    when(this.userService.register(registerDTO)).thenThrow(new UserAlreadyExistsException());

    this.mockMvc.perform(post(this.baseUrl + "/auth/register")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
      .andExpect(jsonPath("$.message").value("Usuário já cadastrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.userService, times(1)).register(registerDTO);
  }

  @Test
  @DisplayName("login - Should return a success response with OK status code, user's info and an access token")
  void userLoginSuccess() throws Exception {
    User mockUser = this.dataMock.getUsers().get(1);
    LoginDTO login = new LoginDTO("teste2@email.com", "123456");
    UserResponseDTO userResponseDTO = new UserResponseDTO(
      mockUser.getId(),
      mockUser.getName(),
      login.email(),
      mockUser.getRole().getName(),
      mockUser.getCreatedAt(),
      mockUser.getUpdatedAt()
    );
    String jsonBody = this.objectMapper.writeValueAsString(login);
    String token = "Access Token";

    Map<String, Object> loginResponseMap = new HashMap<>();
    loginResponseMap.put("user", mockUser);
    loginResponseMap.put("token", token);

    when(this.userService.login(login)).thenReturn(loginResponseMap);

    this.mockMvc.perform(post(this.baseUrl + "/auth/login")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Usuário logado"))
      .andExpect(jsonPath("$.data.userInfo.id").value(userResponseDTO.id()))
      .andExpect(jsonPath("$.data.userInfo.name").value(userResponseDTO.name()))
      .andExpect(jsonPath("$.data.userInfo.email").value(userResponseDTO.email()))
      .andExpect(jsonPath("$.data.userInfo.role").value(userResponseDTO.role()))
      .andExpect(jsonPath("$.data.userInfo.createdAt").value(userResponseDTO.createdAt().toString()))
      .andExpect(jsonPath("$.data.userInfo.updatedAt").value(userResponseDTO.updatedAt().toString()))
      .andExpect(jsonPath("$.data.token").value(token));

    verify(this.userService, times(1)).login(login);
  }

  @Test
  @DisplayName("login - Should return an error response with a not found status code")
  void userLoginFailsByUserNotFound() throws Exception {
    LoginDTO login = new LoginDTO("teste1@email.com", "123456");
    String jsonBody = this.objectMapper.writeValueAsString(login);

    when(this.userService.login(login)).thenThrow(new RecordNotFoundException("Usuário não encontrado"));

    this.mockMvc.perform(post(this.baseUrl + "/auth/login")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Usuário não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.userService, times(1)).login(login);
  }

  @Test
  @DisplayName("login - Should return an error response with an unauthorized status code")
  void userLoginFailsByBadCredentials() throws Exception {
    LoginDTO login = new LoginDTO("teste1@email.com", "123456");
    String jsonBody = this.objectMapper.writeValueAsString(login);

    when(this.userService.login(login)).thenThrow(new BadCredentialsException("Usuário ou senha inválidos"));

    this.mockMvc.perform(post(this.baseUrl + "/auth/login")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
      .andExpect(jsonPath("$.message").value("Usuário ou senha inválidos"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.userService, times(1)).login(login);
  }

  @Test
  @DisplayName("getAllUsers - Should return a success response with OK status code and a list of UserResponseDTO")
  void getAllUsersSuccess() throws Exception {
    List<User> users = this.dataMock.getUsers();
    List<UserResponseDTO> usersResponse = users
      .stream()
      .map(user -> new UserResponseDTO(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getRole().getName(),
        user.getCreatedAt(),
        user.getUpdatedAt()))
      .toList();

    CustomResponseBody<List<UserResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os usuários");
    response.setData(usersResponse);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.userService.getAllUsers()).thenReturn(users);

    this.mockMvc.perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.userService, times(1)).getAllUsers();
  }

  @Test
  @DisplayName("getAuthenticatedUserProfile - Should return a success response with OK status code and the authenticated user's info")
  void getAuthenticatedUserProfileSuccess() throws Exception {
    User user = this.dataMock.getUsers().get(1);
    UserResponseDTO userDTO = new UserResponseDTO(
      user.getId(),
      user.getName(),
      user.getEmail(),
      user.getRole().getName(),
      user.getCreatedAt(),
      user.getUpdatedAt()
    );

    when(this.userService.getAuthenticatedUserProfile()).thenReturn(user);

    this.mockMvc.perform(get(this.baseUrl + "/users/me")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Usuário autenticado"))
      .andExpect(jsonPath("$.data.id").value(userDTO.id()))
      .andExpect(jsonPath("$.data.name").value(userDTO.name()))
      .andExpect(jsonPath("$.data.email").value(userDTO.email()))
      .andExpect(jsonPath("$.data.role").value(userDTO.role()))
      .andExpect(jsonPath("$.data.createdAt").value(userDTO.createdAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(userDTO.updatedAt().toString()));

    verify(this.userService, times(1)).getAuthenticatedUserProfile();
  }

  @Test
  @DisplayName("getProfile - Should return a success response with OK status code and the user's info")
  void getUserProfileSuccess() throws Exception {
    User user = this.dataMock.getUsers().get(1);
    UserResponseDTO userResponse = new UserResponseDTO(
      user.getId(),
      user.getName(),
      user.getEmail(),
      user.getRole().getName(),
      user.getCreatedAt(),
      user.getUpdatedAt()
    );

    when(this.userService.getProfile("02")).thenReturn(user);

    this.mockMvc.perform(get(this.baseUrl + "/users/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Usuário encontrado"))
      .andExpect(jsonPath("$.data.id").value(userResponse.id()))
      .andExpect(jsonPath("$.data.name").value(userResponse.name()))
      .andExpect(jsonPath("$.data.email").value(userResponse.email()))
      .andExpect(jsonPath("$.data.role").value(userResponse.role()))
      .andExpect(jsonPath("$.data.createdAt").value(userResponse.createdAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(userResponse.updatedAt().toString()));

    verify(this.userService, times(1)).getProfile("02");
  }

  @Test
  @DisplayName("getProfile - Should return an error response with not found status code")
  void getUserProfileFailsByUserNotFound() throws Exception {
    when(this.userService.getProfile("02")).thenThrow(new RecordNotFoundException("Usuário não encontrado"));

    this.mockMvc.perform(get(this.baseUrl + "/users/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Usuário não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.userService, times(1)).getProfile("02");
  }

  @Test
  @DisplayName("updateAuthenticatedUser - Should return a success response with OK status code and the updated user")
  void updateAuthenticatedUserSuccess() throws Exception {
    User user = this.dataMock.getUsers().get(1);
    UserResponseDTO updatedUser = new UserResponseDTO(
      user.getId(),
      user.getName(),
      user.getEmail(),
      user.getRole().getName(),
      user.getCreatedAt(),
      user.getUpdatedAt()
    );
    UserUpdateDTO userData = new UserUpdateDTO("User 2", "123456");
    String jsonBody = this.objectMapper.writeValueAsString(userData);

    when(this.userService.updateAuthenticatedUser("02", userData)).thenReturn(user);

    this.mockMvc.perform(patch(this.baseUrl + "/users/02")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Usuário atualizado com sucesso"))
      .andExpect(jsonPath("$.data.id").value(updatedUser.id()))
      .andExpect(jsonPath("$.data.name").value(updatedUser.name()))
      .andExpect(jsonPath("$.data.email").value(updatedUser.email()))
      .andExpect(jsonPath("$.data.role").value(updatedUser.role()))
      .andExpect(jsonPath("$.data.createdAt").value(updatedUser.createdAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(updatedUser.updatedAt().toString()));

    verify(this.userService, times(1)).updateAuthenticatedUser("02", userData);
  }

  @Test
  @DisplayName("updateAuthenticatedUser - Should return an error response with forbidden status code")
  void updateAuthenticatedUserFailsByDifferentUserId() throws Exception {
    UserUpdateDTO updateDTO = new UserUpdateDTO("Updated User", "123456");
    String jsonBody = this.objectMapper.writeValueAsString(updateDTO);

    when(this.userService.updateAuthenticatedUser("01", updateDTO)).thenThrow(new AccessDeniedException("Acesso negado"));

    this.mockMvc.perform(patch(this.baseUrl + "/users/01")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.userService, times(1)).updateAuthenticatedUser("01", updateDTO);
  }

  @Test
  @DisplayName("updateAuthenticatedUser - Should return an error response with not found status code")
  void updateAuthenticatedUserFailsByUserNotFound() throws Exception {
    UserUpdateDTO userData = new UserUpdateDTO("User", "123456");
    String jsonBody = this.objectMapper.writeValueAsString(userData);

    when(this.userService.updateAuthenticatedUser("01", userData)).thenThrow(new RecordNotFoundException("Usuário não encontrado"));

    this.mockMvc.perform(patch(this.baseUrl + "/users/01")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Usuário não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.userService, times(1)).updateAuthenticatedUser("01", userData);
  }

  @Test
  @DisplayName("updateRole - Should return a success response with OK status code and user's info")
  void updateUserRoleSuccess() throws Exception {
    UserRoleUpdateDTO roleData = new UserRoleUpdateDTO("WRITE_READ");
    User user = this.dataMock.getUsers().get(2);
    user.setRole(this.userMapper.convertValueToRole(roleData.role()));

    UserResponseDTO userResponse = new UserResponseDTO(
      user.getId(),
      user.getName(),
      user.getEmail(),
      user.getRole().getName(),
      user.getCreatedAt(),
      user.getUpdatedAt()
    );
    String jsonBody = this.objectMapper.writeValueAsString(roleData);

    when(this.userService.updateRole("03", roleData)).thenReturn(user);

    this.mockMvc.perform(patch(this.baseUrl + "/users/03/role")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Role atualizada com sucesso"))
      .andExpect(jsonPath("$.data.id").value(userResponse.id()))
      .andExpect(jsonPath("$.data.name").value(userResponse.name()))
      .andExpect(jsonPath("$.data.email").value(userResponse.email()))
      .andExpect(jsonPath("$.data.role").value(userResponse.role()))
      .andExpect(jsonPath("$.data.createdAt").value(userResponse.createdAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(userResponse.createdAt().toString()));

    verify(this.userService, times(1)).updateRole("03", roleData);
  }

  @Test
  @DisplayName("updateRole - Should return an error response with not found status code if user is not found")
  void updateUserRoleFailsByUserNotFound() throws Exception {
    UserRoleUpdateDTO roleData = new UserRoleUpdateDTO("WRITE_READ");
    String jsonBody = this.objectMapper.writeValueAsString(roleData);

    when(this.userService.updateRole("03", roleData)).thenThrow(new RecordNotFoundException("Usuário não encontrado"));

    this.mockMvc.perform(patch(this.baseUrl + "/users/03/role")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Usuário não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.userService, times(1)).updateRole("03", roleData);
  }

  @Test
  @DisplayName("delete - Should return a success response with OK status code")
  void deleteUserSuccess() throws Exception {
    User user = this.dataMock.getUsers().get(1);
    UserResponseDTO deletedUser = new UserResponseDTO(
      user.getId(),
      user.getName(),
      user.getEmail(),
      user.getRole().getName(),
      user.getCreatedAt(),
      user.getUpdatedAt()
    );
    Map<String, User> response = new HashMap<>();
    response.put("deletedUser", user);

    when(this.userService.delete("02")).thenReturn(response);

    this.mockMvc.perform(delete(this.baseUrl + "/users/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Usuário deletado com sucesso"))
      .andExpect(jsonPath("$.data.deletedUser.id").value(deletedUser.id()))
      .andExpect(jsonPath("$.data.deletedUser.name").value(deletedUser.name()))
      .andExpect(jsonPath("$.data.deletedUser.email").value(deletedUser.email()))
      .andExpect(jsonPath("$.data.deletedUser.role").value(deletedUser.role()))
      .andExpect(jsonPath("$.data.deletedUser.createdAt").value(deletedUser.createdAt().toString()))
      .andExpect(jsonPath("$.data.deletedUser.updatedAt").value(deletedUser.updatedAt().toString()));

    verify(this.userService, times(1)).delete("02");
  }

  @Test
  @DisplayName("delete - Should return an error response with not found status code")
  void deleteUserFailsByUserNotFound() throws Exception {
    when(this.userService.delete("02")).thenThrow(new RecordNotFoundException("Usuário não encontrado"));

    this.mockMvc.perform(delete(this.baseUrl + "/users/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Usuário não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.userService, times(1)).delete("02");
  }
}
