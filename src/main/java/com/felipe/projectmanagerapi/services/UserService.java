package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.UserRegisterDTO;
import com.felipe.projectmanagerapi.dtos.UserResponseDTO;
import com.felipe.projectmanagerapi.dtos.mappers.UserMapper;
import com.felipe.projectmanagerapi.infra.security.UserPrincipal;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
  }

  public UserResponseDTO register(UserRegisterDTO data) throws RuntimeException {
    Optional<User> existingUser = this.userRepository.findByEmail(data.email());

    if(existingUser.isPresent()) {
      throw new RuntimeException("Usuário já cadastrado");
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

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return this.userRepository.findByEmail(username)
      .map(UserPrincipal::new)
      .orElseThrow(() -> new UsernameNotFoundException("Usuário '" + username + "' não encontrado."));
  }
}
