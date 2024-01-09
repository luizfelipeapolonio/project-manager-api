package com.felipe.projectmanagerapi.system;

import com.felipe.projectmanagerapi.enums.Role;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
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

  // TODO: Não acessar userRepository diretamente. Acessar via service (talvez)
  private final UserRepository userRepository;

  public DBDataInitializer(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void run(String... args) throws Exception {
    Optional<User> admin = this.userRepository.findByEmail(this.email);
    if(admin.isPresent()) return;

    User superUser = new User();
    superUser.setName(this.user);
    superUser.setEmail(this.email);
    superUser.setPassword(this.password);
    superUser.setRole(Role.ADMIN);

    this.userRepository.save(superUser);
  }
}
