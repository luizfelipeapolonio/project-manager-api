package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.LoginDTO;
import com.felipe.projectmanagerapi.dtos.UserRegisterDTO;
import com.felipe.projectmanagerapi.dtos.UserResponseDTO;
import com.felipe.projectmanagerapi.dtos.mappers.UserMapper;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.exceptions.UserAlreadyExistsException;
import com.felipe.projectmanagerapi.infra.security.TokenService;
import com.felipe.projectmanagerapi.infra.security.UserPrincipal;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.repositories.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

  public UserService(
    UserRepository userRepository,
    UserMapper userMapper,
    PasswordEncoder passwordEncoder,
    AuthenticationManager authenticationManager,
    TokenService tokenService
  ) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.tokenService = tokenService;
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
}
