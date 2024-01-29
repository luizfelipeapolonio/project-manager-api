package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.*;
import com.felipe.projectmanagerapi.dtos.mappers.UserMapper;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.exceptions.UserAlreadyExistsException;
import com.felipe.projectmanagerapi.infra.security.AuthorizationService;
import com.felipe.projectmanagerapi.infra.security.TokenService;
import com.felipe.projectmanagerapi.infra.security.UserPrincipal;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.repositories.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;
  private final AuthorizationService authorizationService;

  public UserService(
    UserRepository userRepository,
    UserMapper userMapper,
    PasswordEncoder passwordEncoder,
    AuthenticationManager authenticationManager,
    TokenService tokenService,
    AuthorizationService authorizationService
  ) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.tokenService = tokenService;
    this.authorizationService = authorizationService;
  }

  public UserResponseDTO register(@Valid @NotNull UserRegisterDTO data) {
    Optional<User> existingUser = this.userRepository.findByEmail(data.email());

    if(existingUser.isPresent()) {
      throw new UserAlreadyExistsException();
    }

    String encodedPassword = this.passwordEncoder.encode(data.password());

    User user = new User();
    user.setName(data.name());
    user.setEmail(data.email());
    user.setPassword(encodedPassword);
    user.setRole(this.userMapper.convertValueToRole(data.role()));

    User createdUser = this.userRepository.save(user);

    return this.userMapper.toDTO(createdUser);
  }

  public Map<String, Object> login(@Valid @NotNull LoginDTO login) {
    try {
      Authentication usernameAndPasswordAuth = new UsernamePasswordAuthenticationToken(login.email(), login.password());
      Authentication authentication = this.authenticationManager.authenticate(usernameAndPasswordAuth);
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      String token = this.tokenService.generateToken(userPrincipal);

      UserResponseDTO user = this.userRepository.findByEmail(login.email())
        .map(this.userMapper::toDTO)
        .orElseThrow(() -> new RecordNotFoundException("Usuário '" + login.email() + "' não encontrado"));

      Map<String, Object> loginResponse = new HashMap<>();
      loginResponse.put("userInfo", user);
      loginResponse.put("token", token);

      return loginResponse;

    } catch(BadCredentialsException e) {
      throw new BadCredentialsException("Usuário ou senha inválidos", e);
    }
  }

  public List<UserResponseDTO> getAllUsers() {
    return this.userRepository.findAll()
      .stream()
      .map(this.userMapper::toDTO)
      .toList();
  }

  public UserResponseDTO getAuthenticatedUserProfile() {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal authenticatedUser = (UserPrincipal) authentication.getPrincipal();
    return this.userMapper.toDTO(authenticatedUser.getUser());
  }

  public UserResponseDTO getProfile(String userId) {
    return this.userRepository.findById(userId)
      .map(this.userMapper::toDTO)
      .orElseThrow(() -> new RecordNotFoundException("Usuário não encontrado"));
  }

  public UserResponseDTO updateAuthenticatedUser(@NotNull String userId, @Valid @NotNull UserUpdateDTO userData) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    if(!userId.equals(userPrincipal.getUser().getId())) {
      throw new AccessDeniedException("Acesso negado");
    }

    return this.userRepository.findByEmail(userPrincipal.getUsername())
      .map(user -> {
        if(userData.name() != null) {
          user.setName(userData.name());
        }
        if(userData.password() != null) {
          user.setPassword(this.passwordEncoder.encode(userData.password()));
        }
        User updatedUser = this.userRepository.save(user);
        return this.userMapper.toDTO(updatedUser);
      })
      .orElseThrow(() -> new RecordNotFoundException("Usuário não encontrado"));
  }

  public UserResponseDTO updateRole(@NotNull String userId, @Valid @NotNull UserRoleUpdateDTO roleDTO) {
    return this.userRepository.findById(userId)
      .map(user -> {
        user.setRole(this.userMapper.convertValueToRole(roleDTO.role()));
        User updatedUser = this.userRepository.save(user);
        return this.userMapper.toDTO(updatedUser);
      })
      .orElseThrow(() -> new RecordNotFoundException("Usuário não encontrado"));
  }

  public Map<String, UserResponseDTO> delete(@NotNull String userId) {
    UserResponseDTO user = this.userRepository.findById(userId)
      .map(this.userMapper::toDTO)
      .orElseThrow(() -> new RecordNotFoundException("Usuário não encontrado"));

    this.userRepository.deleteById(user.id());

    Map<String, UserResponseDTO> deletedUser = new HashMap<>();
    deletedUser.put("deletedUser", user);
    return deletedUser;
  }
}
