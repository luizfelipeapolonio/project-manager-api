package com.felipe.projectmanagerapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipe.projectmanagerapi.dtos.LoginDTO;
import com.felipe.projectmanagerapi.dtos.UserRegisterDTO;
import com.felipe.projectmanagerapi.dtos.UserResponseDTO;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.enums.Role;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.exceptions.UserAlreadyExistsException;
import com.felipe.projectmanagerapi.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

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

  private AutoCloseable closeable;
  private String baseUrl;
  private LocalDateTime mockDateTime;

  @BeforeEach
  void setUp() {
    this.closeable = MockitoAnnotations.openMocks(this);
    this.baseUrl = "/api";
    this.mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");
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
    UserResponseDTO createdUser = new UserResponseDTO(
      "01",
      data.name(),
      data.email(),
      data.role(),
      this.mockDateTime,
      this.mockDateTime
    );
    String jsonBody = this.objectMapper.writeValueAsString(data);

    when(this.userService.register(data)).thenReturn(createdUser);

    this.mockMvc.perform(post(this.baseUrl + "/auth/register")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.CREATED.value()))
      .andExpect(jsonPath("$.message").value("Usuário criado com sucesso"))
      .andExpect(jsonPath("$.data.id").value(createdUser.id()))
      .andExpect(jsonPath("$.data.name").value(createdUser.name()))
      .andExpect(jsonPath("$.data.email").value(createdUser.email()))
      .andExpect(jsonPath("$.data.role").value(createdUser.role()))
      .andExpect(jsonPath("$.data.createdAt").value(createdUser.createdAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(createdUser.updatedAt().toString()));

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
    LoginDTO login = new LoginDTO("teste1@email.com", "123456");
    UserResponseDTO userResponseDTO = new UserResponseDTO(
      "01",
      "User 1",
      login.email(),
      Role.READ_ONLY.getName(),
      this.mockDateTime,
      this.mockDateTime
    );
    String jsonBody = this.objectMapper.writeValueAsString(login);
    String token = "Access Token";

    Map<String, Object> response = new HashMap<>();
    response.put("userInfo", userResponseDTO);
    response.put("token", token);

    when(this.userService.login(login)).thenReturn(response);

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
}