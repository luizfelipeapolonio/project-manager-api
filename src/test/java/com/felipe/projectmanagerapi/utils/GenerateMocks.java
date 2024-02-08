package com.felipe.projectmanagerapi.utils;

import com.felipe.projectmanagerapi.enums.Role;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.models.Workspace;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GenerateMocks {

  private final List<User> users;
  private final List<Workspace> workspaces;

  public GenerateMocks() {
    this.users = this.generateUsers();
    this.workspaces = this.generateWorkspaces();
  }

  public List<User> getUsers() {
    return this.users;
  }

  public List<Workspace> getWorkspaces() {
    return this.workspaces;
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
}
