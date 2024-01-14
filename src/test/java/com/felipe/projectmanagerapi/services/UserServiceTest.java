package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.UserRegisterDTO;
import com.felipe.projectmanagerapi.dtos.UserResponseDTO;
import com.felipe.projectmanagerapi.dtos.mappers.UserMapper;
import com.felipe.projectmanagerapi.enums.Role;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

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
  @DisplayName("loadUserByUsername - Should successfully return a UserDetails instance when the user's email is provided")
  void loadUserByUsernameSuccess() {
    User user = new User();
    user.setId("01");
    user.setName("User 1");
    user.setEmail("user1@email.com");
    user.setPassword("123456");
    user.setRole(Role.WRITE_READ);

    when(this.userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

    UserDetails userPrincipal = this.userService.loadUserByUsername(user.getEmail());

    assertThat(userPrincipal.getUsername()).isEqualTo(user.getEmail());
    assertThat(userPrincipal.getPassword()).isEqualTo(user.getPassword());

    verify(this.userRepository, times(1)).findByEmail(user.getEmail());
  }

  @Test
  @DisplayName("loadUserByUsername - Should throw a UsernameNotFoundException if a user with the provided email is not found")
  void loadUserByUsernameFailsByUserNotFound() {
    User user = new User(
      "01",
      "User 1",
      "user1@email.com",
      "123456",
      Role.READ_ONLY
    );

    when(this.userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.userService.loadUserByUsername(user.getEmail()));

    assertThat(thrown)
      .isExactlyInstanceOf(UsernameNotFoundException.class)
      .hasMessage("Usuário '" + user.getEmail() + "' não encontrado.");

    verify(this.userRepository, times(1)).findByEmail(user.getEmail());
  }

}
