package com.felipe.projectmanagerapi.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workspace")
public class Workspace {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(length = 100, nullable = false)
  private String name;

  @CreationTimestamp
  @Column(name = "created_at", columnDefinition = "TIMESTAMP(2)", nullable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", columnDefinition = "TIMESTAMP(2)", nullable = false)
  private LocalDateTime updatedAt;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
    name = "workspace_members",
    joinColumns = @JoinColumn(name = "workspace_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private List<User> members = new ArrayList<>();

  @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL)
  private List<Project> projects = new ArrayList<>();

  public Workspace() {}

  public Workspace(String id, String name, LocalDateTime createdAt, LocalDateTime updatedAt, User owner, List<User> members) {
    this.id = id;
    this.name = name;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.owner = owner;
    this.members = members;
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

  public User getOwner() {
    return this.owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
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

  public List<User> getMembers() {
    return this.members;
  }

  public void setMembers(List<User> members) {
    this.members = members;
  }

  public void addMember(User user) {
    this.members.add(user);
  }

  public void removeMember(User user) {
    this.members.remove(user);
  }

  public List<Project> getProjects() {
    return this.projects;
  }

  public void setProjects(List<Project> projects) {
    this.projects = projects;
  }
}
