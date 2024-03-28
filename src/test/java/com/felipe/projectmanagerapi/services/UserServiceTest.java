package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.*;
import com.felipe.projectmanagerapi.dtos.mappers.UserMapper;
import com.felipe.projectmanagerapi.enums.Role;
import com.felipe.projectmanagerapi.exceptions.ExistingResourcesException;
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
import static org.mockito.Mockito.doNothing;
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

    User createdUser = this.userService.register(userData);

    assertThat(createdUser.getId()).isEqualTo(user.getId());
    assertThat(createdUser.getName()).isEqualTo(user.getName());
    assertThat(createdUser.getEmail()).isEqualTo(user.getEmail());
    assertThat(createdUser.getRole()).isEqualTo(user.getRole());
    assertThat(createdUser.getCreatedAt()).isEqualTo(user.getCreatedAt());
    assertThat(createdUser.getUpdatedAt()).isEqualTo(user.getUpdatedAt());

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

    when(this.userRepository.findByEmail(login.email())).thenReturn(Optional.of(user));
    when(this.authenticationManager.authenticate(auth)).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.tokenService.generateToken(userPrincipal)).thenReturn("Access Token");

    Map<String, Object> loginResponse = this.userService.login(login);

    assertThat(loginResponse.containsKey("user")).isTrue();
    assertThat(loginResponse.containsKey("token")).isTrue();
    assertThat(loginResponse.get("user"))
      .extracting("id", "name", "email", "password", "role", "createdAt", "updatedAt")
      .contains(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getPassword(),
        user.getRole(),
        user.getCreatedAt(),
        user.getUpdatedAt()
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

    List<User> allUsers = this.userService.getAllUsers();

    assertThat(allUsers).hasSize(3);
    assertThat(allUsers.get(0).getId()).isEqualTo(users.get(0).getId());
    assertThat(allUsers.get(0).getName()).isEqualTo(users.get(0).getName());
    assertThat(allUsers.get(0).getEmail()).isEqualTo(users.get(0).getEmail());
    assertThat(allUsers.get(1).getId()).isEqualTo(users.get(1).getId());
    assertThat(allUsers.get(1).getName()).isEqualTo(users.get(1).getName());
    assertThat(allUsers.get(1).getEmail()).isEqualTo(users.get(1).getEmail());
    assertThat(allUsers.get(2).getId()).isEqualTo(users.get(2).getId());
    assertThat(allUsers.get(2).getName()).isEqualTo(users.get(2).getName());
    assertThat(allUsers.get(2).getEmail()).isEqualTo(users.get(2).getEmail());

    verify(this.userRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("getAuthenticatedUserProfile - Should successfully return the authenticated user's profile")
  void getAuthenticatedUserProfileSuccess() {
    User user = this.dataMock.getUsers().get(0);
    UserPrincipal userPrincipal = new UserPrincipal(user);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);

    User authenticatedUser = this.userService.getAuthenticatedUserProfile();

    assertThat(authenticatedUser.getId()).isEqualTo(userPrincipal.getUser().getId());
    assertThat(authenticatedUser.getName()).isEqualTo(userPrincipal.getUser().getName());
    assertThat(authenticatedUser.getEmail()).isEqualTo(userPrincipal.getUsername());
    assertThat(authenticatedUser.getRole().getName()).isEqualTo(userPrincipal.getUser().getRole().getName());
    assertThat(authenticatedUser.getCreatedAt()).isEqualTo(userPrincipal.getUser().getCreatedAt());
    assertThat(authenticatedUser.getUpdatedAt()).isEqualTo(userPrincipal.getUser().getUpdatedAt());

    verify(this.authentication, times(1)).getPrincipal();
  }

  @Test
  @DisplayName("getProfile - Should successfully return a user profile")
  void getProfileSuccess() {
    User user = this.dataMock.getUsers().get(1);

    when(this.userRepository.findById("02")).thenReturn(Optional.of(user));

    User userProfile = this.userService.getProfile("02");

    assertThat(userProfile.getId()).isEqualTo(user.getId());
    assertThat(userProfile.getName()).isEqualTo(user.getName());
    assertThat(userProfile.getEmail()).isEqualTo(user.getEmail());
    assertThat(userProfile.getRole()).isEqualTo(user.getRole());
    assertThat(userProfile.getCreatedAt()).isEqualTo(user.getCreatedAt());
    assertThat(userProfile.getUpdatedAt()).isEqualTo(user.getUpdatedAt());

    verify(this.userRepository, times(1)).findById("02");
  }

  @Test
  @DisplayName("getProfile - Should throw a RecordNotFoundException if user is not found")
  void getProfileFailsByUserNotFound() {
    when(this.userRepository.findById("02")).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.userService.getProfile("02"));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Usuário não encontrado");

    verify(this.userRepository, times(1)).findById("02");
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

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.userRepository.findByEmail(userPrincipal.getUsername())).thenReturn(Optional.of(user));
    when(this.userRepository.save(any(User.class))).thenReturn(updatedUserEntity);

    User updatedUser = this.userService.updateAuthenticatedUser("02", updateDTO);

    assertThat(updatedUser.getId()).isEqualTo(user.getId());
    assertThat(updatedUser.getName()).isEqualTo(updatedUserEntity.getName());
    assertThat(updatedUser.getEmail()).isEqualTo(updatedUserEntity.getEmail());
    assertThat(updatedUser.getPassword()).isEqualTo(updatedUserEntity.getPassword());
    assertThat(updatedUser.getRole()).isEqualTo(updatedUserEntity.getRole());
    assertThat(updatedUser.getCreatedAt()).isEqualTo(updatedUserEntity.getCreatedAt());
    assertThat(updatedUser.getUpdatedAt()).isEqualTo(updatedUserEntity.getUpdatedAt());

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
  }

  @Test
  @DisplayName("updateRole - Should successfully update the user role")
  void updateUserRoleSuccess() {
    User user = this.dataMock.getUsers().get(2);
    UserRoleUpdateDTO updateRoleDTO = new UserRoleUpdateDTO("WRITE_READ");

    User updatedUser = this.dataMock.getUsers().get(2);
    updatedUser.setRole(Role.WRITE_READ);

    when(this.userRepository.findById("03")).thenReturn(Optional.of(user));
    when(this.userRepository.save(any(User.class))).thenReturn(updatedUser);

    User updatedUserResponse = this.userService.updateRole("03", updateRoleDTO);

    assertThat(updatedUserResponse.getId()).isEqualTo(updatedUser.getId());
    assertThat(updatedUserResponse.getName()).isEqualTo(updatedUser.getName());
    assertThat(updatedUserResponse.getEmail()).isEqualTo(updatedUser.getEmail());
    assertThat(updatedUserResponse.getPassword()).isEqualTo(updatedUser.getPassword());
    assertThat(updatedUserResponse.getRole()).isEqualTo(updatedUser.getRole());
    assertThat(updatedUserResponse.getCreatedAt()).isEqualTo(updatedUser.getCreatedAt());
    assertThat(updatedUserResponse.getUpdatedAt()).isEqualTo(updatedUser.getUpdatedAt());

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

  @Test
  @DisplayName("delete - Should successfully delete a user")
  void deleteUserSuccess() {
    User user = this.dataMock.getUsers().get(1);

    when(this.userRepository.findById("02")).thenReturn(Optional.of(user));
    doNothing().when(this.userRepository).deleteById("02");

    Map<String, User> deletedUser = this.userService.delete("02");

    assertThat(deletedUser.containsKey("deletedUser")).isTrue();
    assertThat(deletedUser.get("deletedUser").getId()).isEqualTo(user.getId());
    assertThat(deletedUser.get("deletedUser").getName()).isEqualTo(user.getName());
    assertThat(deletedUser.get("deletedUser").getEmail()).isEqualTo(user.getEmail());
    assertThat(deletedUser.get("deletedUser").getPassword()).isEqualTo(user.getPassword());
    assertThat(deletedUser.get("deletedUser").getRole()).isEqualTo(user.getRole());
    assertThat(deletedUser.get("deletedUser").getCreatedAt()).isEqualTo(user.getCreatedAt());
    assertThat(deletedUser.get("deletedUser").getUpdatedAt()).isEqualTo(user.getUpdatedAt());

    verify(this.userRepository, times(1)).findById("02");
    verify(this.userRepository, times(1)).deleteById("02");
  }

  @Test
  @DisplayName("delete - Should throw a RecordNotFoundException if user is not found")
  void deleteUserFailsByUserNotFound() {
    when(this.userRepository.findById("02")).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.userService.delete("02"));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Usuário não encontrado");

    verify(this.userRepository, times(1)).findById("02");
    verify(this.userRepository, never()).deleteById("02");
  }

  @Test
  @DisplayName("delete - Should throw an ExistingResourcesException if user has workspaces")
  void deleteUserFailsByExistingWorkspace() {
    User user = this.dataMock.getUsers().get(1);
    user.setMyWorkspaces(List.of(this.dataMock.getWorkspaces().get(1)));

    when(this.userRepository.findById("02")).thenReturn(Optional.of(user));

    Exception thrown = catchException(() -> this.userService.delete("02"));

    assertThat(thrown)
      .isExactlyInstanceOf(ExistingResourcesException.class)
      .hasMessage("Não foi possível excluir! O usuário ainda possui: 1 workspace(s), 0 projeto(s), 0 task(s)");

    verify(this.userRepository, times(1)).findById("02");
    verify(this.userRepository, never()).deleteById(anyString());
  }

  @Test
  @DisplayName("delete - Should throw an ExistingResourcesException if user has projects")
  void deleteUserFailsByExistingProject() {
    User user = this.dataMock.getUsers().get(1);
    user.setMyProjects(List.of(this.dataMock.getProjects().get(1)));

    when(this.userRepository.findById("02")).thenReturn(Optional.of(user));

    Exception thrown = catchException(() -> this.userService.delete("02"));

    assertThat(thrown)
      .isExactlyInstanceOf(ExistingResourcesException.class)
      .hasMessage("Não foi possível excluir! O usuário ainda possui: 0 workspace(s), 1 projeto(s), 0 task(s)");

    verify(this.userRepository, times(1)).findById("02");
    verify(this.userRepository, never()).deleteById(anyString());
  }

  @Test
  @DisplayName("delete - Should throw an ExistingResourcesException if user has tasks")
  void deleteUserFailsByExistingTask() {
    User user = this.dataMock.getUsers().get(1);
    user.setMyTasks(List.of(this.dataMock.getTasks().get(0)));

    when(this.userRepository.findById("02")).thenReturn(Optional.of(user));

    Exception thrown = catchException(() -> this.userService.delete("02"));

    assertThat(thrown)
      .isExactlyInstanceOf(ExistingResourcesException.class)
      .hasMessage("Não foi possível excluir! O usuário ainda possui: 0 workspace(s), 0 projeto(s), 1 task(s)");

    verify(this.userRepository, times(1)).findById("02");
    verify(this.userRepository, never()).deleteById(anyString());
  }
}
