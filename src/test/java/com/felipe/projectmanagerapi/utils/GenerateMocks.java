package com.felipe.projectmanagerapi.utils;

import com.felipe.projectmanagerapi.enums.PriorityLevel;
import com.felipe.projectmanagerapi.enums.Role;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.models.Workspace;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GenerateMocks {

  private final List<User> users;
  private final List<Workspace> workspaces;
  private final List<Project> projects;

  public GenerateMocks() {
    this.users = this.generateUsers();
    this.workspaces = this.generateWorkspaces();
    this.projects = this.generateProjects();
  }

  public List<User> getUsers() {
    return this.users;
  }

  public List<Workspace> getWorkspaces() {
    return this.workspaces;
  }

  public List<Project> getProjects() {
    return this.projects;
  }

  private List<User> generateUsers() {
    List<User> users = new ArrayList<>();

    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");

    User u1 = new User();
    u1.setId("01");
    u1.setName("User 1");
    u1.setEmail("teste1@email.com");
    u1.setPassword("123456");
    u1.setRole(Role.ADMIN);
    u1.setCreatedAt(mockDateTime);
    u1.setUpdatedAt(mockDateTime);

    User u2 = new User();
    u2.setId("02");
    u2.setName("User 2");
    u2.setEmail("teste2@email.com");
    u2.setPassword("123456");
    u2.setRole(Role.WRITE_READ);
    u2.setCreatedAt(mockDateTime);
    u2.setUpdatedAt(mockDateTime);

    User u3 = new User();
    u3.setId("03");
    u3.setName("User 3");
    u3.setEmail("teste3@email.com");
    u3.setPassword("123456");
    u3.setRole(Role.READ_ONLY);
    u3.setCreatedAt(mockDateTime);
    u3.setUpdatedAt(mockDateTime);

    users.add(u1);
    users.add(u2);
    users.add(u3);

    return users;
  }

  private List<Workspace> generateWorkspaces() {
    List<Workspace> workspaces = new ArrayList<>();
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");
    User u1 = this.users.get(0);

    Workspace w1 = new Workspace(
      "01",
      "Workspace 1",
      mockDateTime,
      mockDateTime,
      u1,
      this.users
    );

    Workspace w2 = new Workspace(
      "02",
      "Workspace 2",
      mockDateTime,
      mockDateTime,
      u1,
      this.users
    );

    Workspace w3 = new Workspace(
      "03",
      "Workspace 3",
      mockDateTime,
      mockDateTime,
      u1,
      this.users
    );

    workspaces.add(w1);
    workspaces.add(w2);
    workspaces.add(w3);
    return workspaces;
  }

  private List<Project> generateProjects() {
    List<Project> projects = new ArrayList<>();
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");

    Project p1 = new Project();
    p1.setId("01");
    p1.setName("Projeto 1");
    p1.setCategory("Desenvolvimento");
    p1.setDescription("Projeto de desenvolvimento");
    p1.setBudget(new BigDecimal("999.99"));
    p1.setDeadline(LocalDate.parse("2025-01-01"));
    p1.setPriority(PriorityLevel.LOW);
    p1.setOwner(this.getUsers().get(0));
    p1.setWorkspace(this.getWorkspaces().get(0));
    p1.setCreatedAt(mockDateTime);
    p1.setUpdatedAt(mockDateTime);

    Project p2 = new Project();
    p2.setId("02");
    p2.setName("Projeto 2");
    p2.setCategory("Infra");
    p2.setDescription("Projeto de infraestrutura");
    p2.setBudget(new BigDecimal("1000.00"));
    p2.setDeadline(LocalDate.parse("2025-01-01"));
    p2.setPriority(PriorityLevel.MEDIUM);
    p2.setOwner(this.getUsers().get(1));
    p2.setWorkspace(this.getWorkspaces().get(0));
    p2.setCreatedAt(mockDateTime);
    p2.setUpdatedAt(mockDateTime);

    Project p3 = new Project();
    p3.setId("03");
    p3.setName("Projeto 3");
    p3.setCategory("Marketing");
    p3.setDescription("Projeto de Marketing");
    p3.setBudget(new BigDecimal("1500.00"));
    p3.setDeadline(LocalDate.parse("2025-01-01"));
    p3.setPriority(PriorityLevel.HIGH);
    p3.setOwner(this.getUsers().get(1));
    p3.setWorkspace(this.getWorkspaces().get(0));
    p3.setCreatedAt(mockDateTime);
    p3.setUpdatedAt(mockDateTime);

    projects.add(p1);
    projects.add(p2);
    projects.add(p3);
    return projects;
  }
}
