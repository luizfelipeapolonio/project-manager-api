package com.felipe.projectmanagerapi.repositories;

import com.felipe.projectmanagerapi.models.Project;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles(value = "test")
public class ProjectRepositoryTest {

  @Autowired
  EntityManager entityManager;

  @Autowired
  ProjectRepository projectRepository;

  private final GenerateMocks dataMock = new GenerateMocks();

  @Test
  @DisplayName("findAllByWorkspaceIdAndOwnerId - Should successfully return all projects that belong to the specific workspace and user")
  void findAllByWorkspaceIdAndOwnerIdSuccess() {
    User workspaceOwnerMock = this.dataMock.getUsers().get(0);
    User projectOwnerMock = this.dataMock.getUsers().get(1);
    Workspace workspaceMock = this.dataMock.getWorkspaces().get(0);
    Project projectMock1 = this.dataMock.getProjects().get(1);
    Project projectMock2 = this.dataMock.getProjects().get(2);
    Project projectMock3 = this.dataMock.getProjects().get(0);

    // Creating new instances
    User workspaceOwner = this.generateUserByMock(workspaceOwnerMock);
    User projectOwner = this.generateUserByMock(projectOwnerMock);
    Workspace workspace = this.generateWorkspaceByMock(workspaceMock, workspaceOwner);
    Project project1 = this.generateProjectByMock(projectMock1, workspace, projectOwner);
    Project project2 = this.generateProjectByMock(projectMock2, workspace, projectOwner);
    Project project3 = this.generateProjectByMock(projectMock3, workspace, workspaceOwner);

    this.entityManager.persist(projectOwner);
    this.entityManager.persist(workspaceOwner);
    this.entityManager.persist(workspace);
    this.entityManager.persist(project1);
    this.entityManager.persist(project2);
    this.entityManager.persist(project3);

    List<Project> foundProjects = this.projectRepository
      .findAllByWorkspaceIdAndOwnerId(workspace.getId(), projectOwner.getId());

    assertThat(foundProjects).allSatisfy(project -> {
      assertThat(project.getWorkspace().getId()).isEqualTo(workspace.getId());
      assertThat(project.getOwner().getId()).isEqualTo(projectOwner.getId());
    }).hasSize(2);
  }

  @Test
  @DisplayName("findById - Should successfully find a project by id in a workspace")
  void findByIdSuccess() {
    User workspaceOwnerMock = this.dataMock.getUsers().get(0);
    User projectOwnerMock = this.dataMock.getUsers().get(1);
    Workspace workspaceMock = this.dataMock.getWorkspaces().get(0);
    Project projectMock = this.dataMock.getProjects().get(1);

    User workspaceOwner = this.generateUserByMock(workspaceOwnerMock);
    User projectOwner = this.generateUserByMock(projectOwnerMock);
    Workspace workspace = this.generateWorkspaceByMock(workspaceMock, workspaceOwner);
    Project project = this.generateProjectByMock(projectMock, workspace, projectOwner);

    this.entityManager.persist(workspaceOwner);
    this.entityManager.persist(projectOwner);
    this.entityManager.persist(workspace);
    this.entityManager.persist(project);

    Optional<Project> foundProject = this.projectRepository.findByProjectIdAndWorkspaceId(project.getId(), workspace.getId());

    assertThat(foundProject.isPresent()).isTrue();
    assertThat(foundProject.get().getId()).isEqualTo(project.getId());
    assertThat(foundProject.get().getName()).isEqualTo(project.getName());
    assertThat(foundProject.get().getCategory()).isEqualTo(project.getCategory());
    assertThat(foundProject.get().getDescription()).isEqualTo(project.getDescription());
    assertThat(foundProject.get().getPriority()).isEqualTo(project.getPriority());
    assertThat(foundProject.get().getDeadline()).isEqualTo(project.getDeadline());
    assertThat(foundProject.get().getBudget()).isEqualTo(project.getBudget());
    assertThat(foundProject.get().getCreatedAt()).isEqualTo(project.getCreatedAt());
    assertThat(foundProject.get().getUpdatedAt()).isEqualTo(project.getUpdatedAt());
    assertThat(foundProject.get().getOwner().getId()).isEqualTo(project.getOwner().getId());
    assertThat(foundProject.get().getWorkspace().getId()).isEqualTo(project.getWorkspace().getId());
  }

  private Project generateProjectByMock(Project project, Workspace workspace, User owner) {
    Project newProject = new Project();
    newProject.setName(project.getName());
    newProject.setCategory(project.getCategory());
    newProject.setDescription(project.getDescription());
    newProject.setBudget(project.getBudget());
    newProject.setPriority(project.getPriority());
    newProject.setDeadline(project.getDeadline());
    newProject.setCreatedAt(project.getCreatedAt());
    newProject.setUpdatedAt(project.getUpdatedAt());
    newProject.setOwner(owner);
    newProject.setWorkspace(workspace);
    return newProject;
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