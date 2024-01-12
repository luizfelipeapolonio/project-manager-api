package com.felipe.projectmanagerapi.system;

import com.felipe.projectmanagerapi.enums.Role;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DBDataInitializer implements CommandLineRunner {

  @Value("${super-user.username}")
  private String user;

  @Value("${super-user.email}")
  private String email;

  @Value("${super-user.password}")
  private String password;

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public DBDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) throws Exception {
    Optional<User> existingSuperUser = this.userRepository.findByEmail(this.email);
    if(existingSuperUser.isPresent()) return;

    User superUser = new User();
    superUser.setName(this.user);
    superUser.setEmail(this.email);
    superUser.setPassword(this.passwordEncoder.encode(this.password));
    superUser.setRole(Role.ADMIN);

    this.userRepository.save(superUser);
  }
}
