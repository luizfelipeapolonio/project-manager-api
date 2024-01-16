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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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

}
