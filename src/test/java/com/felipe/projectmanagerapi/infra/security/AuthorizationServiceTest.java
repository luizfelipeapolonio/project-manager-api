package com.felipe.projectmanagerapi.infra.security;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class AuthorizationServiceTest {

  @Autowired
  @InjectMocks
  AuthorizationService authorizationService;

  @Mock
  UserRepository userRepository;

  @Mock
  Authentication authentication;

  @Mock
  SecurityContext securityContext;

  private AutoCloseable closeable;
  private LocalDateTime mockDateTime;

  @BeforeEach
  void setUp() {
    this.closeable = MockitoAnnotations.openMocks(this);
    this.mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");
  }

  @AfterEach
  void tearDown() throws Exception {
    this.closeable.close();
    SecurityContextHolder.clearContext();
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

    UserDetails userPrincipal = this.authorizationService.loadUserByUsername(user.getEmail());

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

    Exception thrown = catchException(() -> this.authorizationService.loadUserByUsername(user.getEmail()));

    assertThat(thrown)
      .isExactlyInstanceOf(UsernameNotFoundException.class)
      .hasMessage("Usuário '" + user.getEmail() + "' não encontrado.");

    verify(this.userRepository, times(1)).findByEmail(user.getEmail());
  }

  @Test
  @DisplayName("getAuthentication - Should return an Authentication object with UserPrincipal that represents the current authenticated user")
  void getAuthenticationSuccess() {
    User user = new User();
    user.setId("01");
    user.setName("User 1");
    user.setEmail("teste1@email.com");
    user.setPassword("123456");
    user.setRole(Role.WRITE_READ);
    user.setCreatedAt(this.mockDateTime);
    user.setUpdatedAt(this.mockDateTime);
    UserPrincipal userPrincipal = new UserPrincipal(user);

    this.mockAuthentication(userPrincipal);

    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal authUser = (UserPrincipal) authentication.getPrincipal();

    assertThat(authUser.getUser().getId()).isEqualTo(userPrincipal.getUser().getId());
    assertThat(authUser.getUser().getName()).isEqualTo(userPrincipal.getUser().getName());
    assertThat(authUser.getUsername()).isEqualTo(userPrincipal.getUsername());
    assertThat(authUser.getPassword()).isEqualTo(userPrincipal.getPassword());
    assertThat(authUser.getUser().getRole()).isEqualTo(userPrincipal.getUser().getRole());
    assertThat(authUser.getUser().getCreatedAt()).isEqualTo(userPrincipal.getUser().getCreatedAt());
    assertThat(authUser.getUser().getUpdatedAt()).isEqualTo(userPrincipal.getUser().getUpdatedAt());
  }

  private void mockAuthentication(UserPrincipal authUser) {
    when(this.authentication.getPrincipal()).thenReturn(authUser);
    when(this.securityContext.getAuthentication()).thenReturn(this.authentication);
    SecurityContextHolder.setContext(this.securityContext);
  }
}
