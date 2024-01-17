package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.LoginDTO;
import com.felipe.projectmanagerapi.dtos.UserRegisterDTO;
import com.felipe.projectmanagerapi.dtos.UserResponseDTO;
import com.felipe.projectmanagerapi.dtos.mappers.UserMapper;
import com.felipe.projectmanagerapi.enums.Role;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.exceptions.UserAlreadyExistsException;
import com.felipe.projectmanagerapi.infra.security.TokenService;
import com.felipe.projectmanagerapi.infra.security.UserPrincipal;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

public class UserServiceTest {

  @Autowired
  @InjectMocks
  UserService userService;

  @Mock
  UserRepository userRepository;

  @Mock
  PasswordEncoder passwordEncoder;

  @Spy
  UserMapper userMapper;

  @Mock
  TokenService tokenService;

  @Mock
  AuthenticationManager authenticationManager;

  @Mock
  Authentication authentication;

  private AutoCloseable closeable;
  private List<User> users;

  @BeforeEach
  void setUp() {
    this.closeable = MockitoAnnotations.openMocks(this);
    this.users = new ArrayList<>();
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");

    User u1 = new User();
    u1.setId("01");
    u1.setName("User 1");
    u1.setEmail("teste1@email.com");
    u1.setPassword("123456");
    u1.setRole(Role.ADMIN);
    u1.setCreatedAt(mockDateTime);
    u1.setUpdatedAt(mockDateTime);

    User u2 = new User();
    u2.setId("02");
    u2.setName("User 2");
    u2.setEmail("teste2@email.com");
    u2.setPassword("123456");
    u2.setRole(Role.WRITE_READ);
    u2.setCreatedAt(mockDateTime);
    u2.setUpdatedAt(mockDateTime);

    User u3 = new User();
    u3.setId("03");
    u3.setName("User 3");
    u3.setEmail("teste3@email.com");
    u3.setPassword("123456");
    u3.setRole(Role.READ_ONLY);
    u3.setCreatedAt(mockDateTime);
    u3.setUpdatedAt(mockDateTime);

    this.users.add(u1);
    this.users.add(u2);
    this.users.add(u3);
  }

  @AfterEach
  void tearDown() throws Exception {
    this.closeable.close();
  }

  @Test
  @DisplayName("userRegister - Should register user successfully and return a UserResponseDTO")
  void userRegisterSuccess() {
    UserRegisterDTO userData = new UserRegisterDTO(
      "User 2",
      "teste2@email.com",
      "123456",
      "WRITE_READ"
    );
    User user = this.users.get(1);

    when(this.passwordEncoder.encode(userData.password())).thenReturn("Encoded Password");
    when(this.userRepository.findByEmail(userData.email())).thenReturn(Optional.empty());
    when(this.userRepository.save(any(User.class))).thenReturn(user);

    UserResponseDTO createdUser = this.userService.register(userData);

    assertThat(createdUser.id()).isEqualTo(user.getId());
    assertThat(createdUser.name()).isEqualTo(user.getName());
    assertThat(createdUser.email()).isEqualTo(user.getEmail());
    assertThat(createdUser.role()).isEqualTo(user.getRole().getName());
    assertThat(createdUser.createdAt()).isEqualTo(user.getCreatedAt());
    assertThat(createdUser.updatedAt()).isEqualTo(user.getUpdatedAt());

    verify(this.passwordEncoder, times(1)).encode(userData.password());
    verify(this.userRepository, times(1)).findByEmail(userData.email());
    verify(this.userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("userRegister - Should throw a UserAlreadyExistsException if the user already exists")
  void userRegisterFailsByAlreadyExistingUser() {
    UserRegisterDTO userData = new UserRegisterDTO(
      "User 2",
      "user2@email.com",
      "123456",
      "WRITE_READ"
    );
    User user = this.users.get(1);

    when(this.userRepository.findByEmail(userData.email())).thenReturn(Optional.of(user));

    Exception thrown = catchException(() -> this.userService.register(userData));

    assertThat(thrown)
      .isExactlyInstanceOf(UserAlreadyExistsException.class)
      .hasMessage("Usuário já cadastrado");

    verify(this.passwordEncoder, never()).encode(anyString());
    verify(this.userRepository, times(1)).findByEmail(userData.email());
    verify(this.userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("login - Should successfully log user in and return the user's info and an access token")
  void userLoginSuccess() {
    LoginDTO login = new LoginDTO("teste1@email.com", "123456");
    Authentication auth = new UsernamePasswordAuthenticationToken(login.email(), login.password());
    User user = this.users.get(0);
    UserPrincipal userPrincipal = new UserPrincipal(user);
    UserResponseDTO userResponseDTO = new UserResponseDTO(
      user.getId(),
      user.getName(),
      user.getEmail(),
      user.getRole().toString(),
      user.getCreatedAt(),
      user.getUpdatedAt()
    );

    when(this.userRepository.findByEmail(login.email())).thenReturn(Optional.of(user));
    when(this.authenticationManager.authenticate(auth)).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.tokenService.generateToken(userPrincipal)).thenReturn("Access Token");

    Map<String, Object> loginResponse = this.userService.login(login);

    assertThat(loginResponse.containsKey("userInfo")).isTrue();
    assertThat(loginResponse.containsKey("token")).isTrue();
    assertThat(loginResponse.get("userInfo"))
      .extracting("id", "name", "email", "role", "createdAt", "updatedAt")
      .contains(
        userResponseDTO.id(),
        userResponseDTO.name(),
        userResponseDTO.email(),
        userResponseDTO.role(),
        userResponseDTO.createdAt(),
        userResponseDTO.updatedAt()
      );
    assertThat(loginResponse.get("token")).isEqualTo("Access Token");

    verify(this.userRepository, times(1)).findByEmail(login.email());
    verify(this.authenticationManager, times(1)).authenticate(auth);
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.tokenService, times(1)).generateToken(userPrincipal);
  }

  @Test
  @DisplayName("login - Should throw a RecordNotFoundException if the provided user doesn't exist")
  void userLoginFailsByUserNotFound() {
    LoginDTO login = new LoginDTO("teste1@email.com", "123456");
    Authentication auth = new UsernamePasswordAuthenticationToken(login.email(), login.password());
    UserPrincipal userPrincipal = new UserPrincipal(this.users.get(0));

    when(this.authenticationManager.authenticate(auth)).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.tokenService.generateToken(userPrincipal)).thenReturn(anyString());
    when(this.userRepository.findByEmail(login.email())).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.userService.login(login));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Usuário '" + login.email() +"' não encontrado");

    verify(this.userRepository, times(1)).findByEmail(login.email());
    verify(this.authenticationManager, times(1)).authenticate(auth);
    verify(this.tokenService, times(1)).generateToken(userPrincipal);
  }

  @Test
  @DisplayName("login - Should throw a BadCredentialsException if login data is inconsistent")
  void userLoginFailsByBadCredentials() {
    LoginDTO login = new LoginDTO("teste1@email.com", "123456");

    when(this.authenticationManager.authenticate(any(Authentication.class))).thenThrow(BadCredentialsException.class);

    Exception thrown = catchException(() -> this.userService.login(login));

    assertThat(thrown)
      .isExactlyInstanceOf(BadCredentialsException.class)
      .hasMessage("Usuário ou senha inválidos");

    verify(this.userRepository, never()).findByEmail(login.email());
    verify(this.authenticationManager, times(1)).authenticate(any(Authentication.class));
    verify(this.tokenService, never()).generateToken(any());
  }
}
