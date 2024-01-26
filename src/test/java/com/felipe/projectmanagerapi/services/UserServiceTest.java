package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.*;
import com.felipe.projectmanagerapi.dtos.mappers.UserMapper;
import com.felipe.projectmanagerapi.enums.Role;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.exceptions.UserAlreadyExistsException;
import com.felipe.projectmanagerapi.infra.security.AuthorizationService;
import com.felipe.projectmanagerapi.infra.security.TokenService;
import com.felipe.projectmanagerapi.infra.security.UserPrincipal;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.repositories.UserRepository;
import com.felipe.projectmanagerapi.utils.GenerateMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

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

  @Mock
  AuthorizationService authorizationService;

  private AutoCloseable closeable;
  private GenerateMocks dataMock;

  @BeforeEach
  void setUp() {
    this.closeable = MockitoAnnotations.openMocks(this);
    this.dataMock = new GenerateMocks();
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
    User user = this.dataMock.getUsers().get(1);

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
    User user = this.dataMock.getUsers().get(1);

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
    User user = this.dataMock.getUsers().get(0);
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
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));

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

  @Test
  @DisplayName("getAllUsers - Should successfully return a list with all users")
  void getAllUsersSuccess() {
    List<User> users = this.dataMock.getUsers();

    when(this.userRepository.findAll()).thenReturn(users);

    List<UserResponseDTO> allUsers = this.userService.getAllUsers();

    assertThat(allUsers).hasSize(3);
    assertThat(allUsers.get(0).id()).isEqualTo(users.get(0).getId());
    assertThat(allUsers.get(0).name()).isEqualTo(users.get(0).getName());
    assertThat(allUsers.get(0).email()).isEqualTo(users.get(0).getEmail());
    assertThat(allUsers.get(1).id()).isEqualTo(users.get(1).getId());
    assertThat(allUsers.get(1).name()).isEqualTo(users.get(1).getName());
    assertThat(allUsers.get(1).email()).isEqualTo(users.get(1).getEmail());
    assertThat(allUsers.get(2).id()).isEqualTo(users.get(2).getId());
    assertThat(allUsers.get(2).name()).isEqualTo(users.get(2).getName());
    assertThat(allUsers.get(2).email()).isEqualTo(users.get(2).getEmail());

    verify(this.userRepository, times(1)).findAll();
    verify(this.userMapper, times(3)).toDTO(any(User.class));
  }

  @Test
  @DisplayName("getAuthenticatedUserProfile - Should successfully return the authenticated user's profile")
  void getAuthenticatedUserProfileSuccess() {
    User user = this.dataMock.getUsers().get(0);
    UserPrincipal userPrincipal = new UserPrincipal(user);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);

    UserResponseDTO authenticatedUser = this.userService.getAuthenticatedUserProfile();

    assertThat(authenticatedUser.id()).isEqualTo(userPrincipal.getUser().getId());
    assertThat(authenticatedUser.name()).isEqualTo(userPrincipal.getUser().getName());
    assertThat(authenticatedUser.email()).isEqualTo(userPrincipal.getUsername());
    assertThat(authenticatedUser.role()).isEqualTo(userPrincipal.getUser().getRole().getName());
    assertThat(authenticatedUser.createdAt()).isEqualTo(userPrincipal.getUser().getCreatedAt());
    assertThat(authenticatedUser.updatedAt()).isEqualTo(userPrincipal.getUser().getUpdatedAt());

    verify(this.authentication, times(1)).getPrincipal();
    verify(this.userMapper, times(1)).toDTO(userPrincipal.getUser());
  }

  @Test
  @DisplayName("updateAuthenticatedUser - Should successfully update authenticated user's info")
  void updateAuthenticatedUserSuccess() {
    User user = this.dataMock.getUsers().get(1);
    UserPrincipal userPrincipal = new UserPrincipal(user);
    UserUpdateDTO updateDTO = new UserUpdateDTO("Updated name", "654321");

    User updatedUserEntity = new User();
    updatedUserEntity.setId(user.getId());
    updatedUserEntity.setName(updateDTO.name());
    updatedUserEntity.setEmail(user.getEmail());
    updatedUserEntity.setPassword(updateDTO.password());
    updatedUserEntity.setRole(user.getRole());
    updatedUserEntity.setCreatedAt(user.getCreatedAt());
    updatedUserEntity.setUpdatedAt(user.getUpdatedAt());
    UserResponseDTO updatedUserResponse = this.userMapper.toDTO(updatedUserEntity);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.userRepository.findByEmail(userPrincipal.getUsername())).thenReturn(Optional.of(user));
    when(this.userRepository.save(any(User.class))).thenReturn(updatedUserEntity);

    UserResponseDTO updatedUser = this.userService.updateAuthenticatedUser("02", updateDTO);

    assertThat(updatedUser.id()).isEqualTo(user.getId());
    assertThat(updatedUser.name()).isEqualTo(updatedUserResponse.name());
    assertThat(updatedUser.email()).isEqualTo(updatedUserResponse.email());
    assertThat(updatedUser.role()).isEqualTo(updatedUserResponse.role());
    assertThat(updatedUser.createdAt()).isEqualTo(updatedUserResponse.createdAt());
    assertThat(updatedUser.updatedAt()).isEqualTo(updatedUserResponse.updatedAt());

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.userRepository, times(1)).findByEmail(userPrincipal.getUsername());
    verify(this.passwordEncoder, times(1)).encode(updateDTO.password());
    verify(this.userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("updateAuthenticatedUser - Should throw an AccessDeniedException if user id is different from authenticated user id")
  void updateAuthenticatedUserFailsByDifferentUserId() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    UserUpdateDTO updateDTO = new UserUpdateDTO("User Update", "654321");

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);

    Exception thrown = catchException(() -> this.userService.updateAuthenticatedUser("03", updateDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.userRepository, never()).findByEmail(anyString());
    verify(this.userRepository, never()).save(any(User.class));
    verify(this.userMapper, never()).toDTO(any(User.class));
  }

  @Test
  @DisplayName("updateAuthenticatedUser - Should throw a RecordNotFoundException if the user is not found")
  void updateAuthenticatedUserFailsByUserNotFound() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    UserUpdateDTO updateDTO = new UserUpdateDTO("Updated Name", "654321");

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.userRepository.findByEmail(userPrincipal.getUsername())).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.userService.updateAuthenticatedUser("02", updateDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Usuário não encontrado");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.userRepository, times(1)).findByEmail(userPrincipal.getUsername());
    verify(this.userRepository, never()).save(any(User.class));
    verify(this.passwordEncoder, never()).encode(anyString());
    verify(this.userMapper, never()).toDTO(any(User.class));
  }

  @Test
  @DisplayName("updateRole - Should successfully update the user role")
  void updateUserRoleSuccess() {
    User user = this.dataMock.getUsers().get(2);
    UserRoleUpdateDTO updateRoleDTO = new UserRoleUpdateDTO("WRITE_READ");

    User updatedUser = this.dataMock.getUsers().get(2);
    updatedUser.setRole(Role.WRITE_READ);

    UserResponseDTO userResponse = this.userMapper.toDTO(updatedUser);

    when(this.userRepository.findById("03")).thenReturn(Optional.of(user));
    when(this.userRepository.save(any(User.class))).thenReturn(updatedUser);

    UserResponseDTO updatedUserResponse = this.userService.updateRole("03", updateRoleDTO);

    assertThat(updatedUserResponse.id()).isEqualTo(userResponse.id());
    assertThat(updatedUserResponse.name()).isEqualTo(userResponse.name());
    assertThat(updatedUserResponse.email()).isEqualTo(userResponse.email());
    assertThat(updatedUserResponse.role()).isEqualTo(userResponse.role());
    assertThat(updatedUserResponse.createdAt()).isEqualTo(userResponse.createdAt());
    assertThat(updatedUserResponse.updatedAt()).isEqualTo(userResponse.updatedAt());

    verify(this.userRepository, times(1)).findById("03");
    verify(this.userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("updateRole - Should throw a RecordNotFoundException if the user does not exist")
  void updateUserRoleFailsByUserNotFound() {
    when(this.userRepository.findById("03")).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.userService.updateRole("03", new UserRoleUpdateDTO("READ_ONLY")));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Usuário não encontrado");

    verify(this.userRepository, times(1)).findById("03");
    verify(this.userRepository, never()).save(any(User.class));
  }
}
