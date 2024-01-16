package com.felipe.projectmanagerapi.infra.security;

import com.felipe.projectmanagerapi.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements UserDetailsService {

  private final UserRepository userRepository;

  public AuthorizationService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return this.userRepository.findByEmail(username)
      .map(UserPrincipal::new)
      .orElseThrow(() -> new UsernameNotFoundException("Usuário '" + username + "' não encontrado."));
  }
}
