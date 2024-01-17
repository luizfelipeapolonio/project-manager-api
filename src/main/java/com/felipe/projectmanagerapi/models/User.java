package com.felipe.projectmanagerapi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.felipe.projectmanagerapi.enums.Role;
import com.felipe.projectmanagerapi.enums.converters.RoleConverter;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(length = 20, nullable = false)
  private String name;

  @Column(nullable = false, unique = true, updatable = false)
  private String email;

  @JsonIgnore
  @Column(nullable = false)
  private String password;

  @Convert(converter = RoleConverter.class)
  @Column(length = 30, nullable = false)
  private Role role = Role.READ_ONLY;

  @CreationTimestamp
  @Column(name = "created_at", columnDefinition = "TIMESTAMP(2)", nullable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", columnDefinition = "TIMESTAMP(2)", nullable = false)
  private LocalDateTime updatedAt;

  public User() {}

  public User(String id, String name, String email, String password, Role role) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.password = password;
    this.role = role;
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Role getRole() {
    return this.role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public LocalDateTime getCreatedAt() {
    return this.createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return this.updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
