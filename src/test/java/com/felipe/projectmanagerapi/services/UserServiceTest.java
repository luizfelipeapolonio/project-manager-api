package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.LoginDTO;
import com.felipe.projectmanagerapi.dtos.UserRegisterDTO;
import com.felipe.projectmanagerapi.dtos.UserResponseDTO;
import com.felipe.projectmanagerapi.dtos.mappers.UserMapper;
import com.felipe.projectmanagerapi.enums.Role;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

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

  @BeforeEach
  void setUp() {
    this.closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void tearDown() throws Exception {
    this.closeable.close();
  }

  @Test
  @DisplayName("userRegister - Should register user successfully and return a UserResponseDTO")
  void userRegisterSuccess() {
    UserRegisterDTO userData = new UserRegisterDTO(
      "User 1",
      "user1@email.com",
      "123456",
      "WRITE_READ"
    );

    User user = new User();
    user.setId("01");
    user.setName(userData.name());
    user.setEmail(userData.email());
    user.setPassword(userData.password());
    user.setRole(this.userMapper.convertValueToRole(userData.role()));

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
  @DisplayName("userRegister - Should throw a RuntimeException if the user already exists")
  void userRegisterFailsByAlreadyExistingUser() {
    UserRegisterDTO userData = new UserRegisterDTO(
      "User 1",
      "user1@email.com",
      "123456",
      "WRITE_READ"
    );

    User user = new User();
    user.setId("01");
    user.setName(userData.name());
    user.setEmail(userData.email());
    user.setPassword(userData.password());
    user.setRole(this.userMapper.convertValueToRole(userData.role()));

    when(this.userRepository.findByEmail(userData.email())).thenReturn(Optional.of(user));

    Exception thrown = catchException(() -> this.userService.register(userData));

    assertThat(thrown)
      .isExactlyInstanceOf(RuntimeException.class)
      .hasMessage("Usuário já cadastrado");

    verify(this.passwordEncoder, never()).encode(anyString());
    verify(this.userRepository, times(1)).findByEmail(userData.email());
    verify(this.userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("login - Should successfully log user in and return the user's info and an access token")
  void userLoginSuccess() {
    LoginDTO login = new LoginDTO("user1@email.com", "123456");
    Authentication auth = new UsernamePasswordAuthenticationToken(login.email(), login.password());

    User user = new User(
      "01",
      "User 1",
      login.email(),
      login.password(),
      Role.WRITE_READ
    );

    UserPrincipal userPrincipal = new UserPrincipal(user);
    UserResponseDTO userResponseDTO = new UserResponseDTO(
      user.getId(),
      user.getName(),
      user.getEmail(),
      user.getRole().toString(),
      user.getCreatedAt(),
      user.getUpdatedAt()
    );

    when(this.authenticationManager.authenticate(auth)).thenReturn(this.authentication);
    // TODO: rever se esse mock de authentication é o correto
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.tokenService.generateToken(any())).thenReturn("Access Token");
    when(this.userRepository.findByEmail(userPrincipal.getUsername())).thenReturn(Optional.of(user));

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
  }
}
