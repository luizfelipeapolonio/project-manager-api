package com.felipe.projectmanagerapi.repositories;

import com.felipe.projectmanagerapi.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
  // TODO: implementar teste unitário para esse método
  Optional<User> findByEmail(String email);
}
