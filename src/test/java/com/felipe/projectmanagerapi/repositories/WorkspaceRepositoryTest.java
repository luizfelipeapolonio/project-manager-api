package com.felipe.projectmanagerapi.repositories;

import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.utils.GenerateMocks;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles(value = "test")
public class WorkspaceRepositoryTest {

  @Autowired
  EntityManager entityManager;

  @Autowired
  WorkspaceRepository workspaceRepository;

  private final GenerateMocks dataMock = new GenerateMocks();

  @Test
  @DisplayName("findAllByOwnerId - Should successfully return all user workspaces")
  void findAllByOwnerIdSuccess() {
    User mockUser1 = this.dataMock.getUsers().get(0);
    User mockUser2 = this.dataMock.getUsers().get(1);
    Workspace mockWorkspace1 = this.dataMock.getWorkspaces().get(0);
    Workspace mockWorkspace2 = this.dataMock.getWorkspaces().get(1);
    Workspace mockWorkspace3 = this.dataMock.getWorkspaces().get(2);

    User user1 = this.generateUserByMock(mockUser1);
    User user2 = this.generateUserByMock(mockUser2);
    Workspace workspace1 = this.generateWorkspaceByMock(mockWorkspace1, user1);
    Workspace workspace2 = this.generateWorkspaceByMock(mockWorkspace2, user1);
    Workspace workspace3 = this.generateWorkspaceByMock(mockWorkspace3, user2);

    this.entityManager.persist(user1);
    this.entityManager.persist(user2);
    this.entityManager.persist(workspace1);
    this.entityManager.persist(workspace2);
    this.entityManager.persist(workspace3);

    List<Workspace> workspaces = this.workspaceRepository.findAllByOwnerId(user1.getId());

    assertThat(workspaces)
      .allSatisfy(workspace -> assertThat(workspace.getOwner().getId()).isEqualTo(user1.getId()))
      .hasSize(2);
  }

  private User generateUserByMock(User user) {
    User generatedUser = new User();
    generatedUser.setName(user.getName());
    generatedUser.setEmail(user.getEmail());
    generatedUser.setPassword(user.getPassword());
    generatedUser.setRole(user.getRole());
    generatedUser.setCreatedAt(user.getCreatedAt());
    generatedUser.setUpdatedAt(user.getUpdatedAt());
    return generatedUser;
  }

  private Workspace generateWorkspaceByMock(Workspace workspace, User user) {
    Workspace generatedWorkspace = new Workspace();
    generatedWorkspace.setName(workspace.getName());
    generatedWorkspace.setOwner(user);
    generatedWorkspace.setCreatedAt(workspace.getCreatedAt());
    generatedWorkspace.setUpdatedAt(workspace.getUpdatedAt());
    return generatedWorkspace;
  }
}
