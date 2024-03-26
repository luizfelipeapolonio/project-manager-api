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
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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
  @DisplayName("findAllByWorkspaceId - Should successfully return all projects from workspace")
  void findAllByWorkspaceIdSuccess() {
    User workspaceOwnerMock = this.dataMock.getUsers().get(0);
    User projectsOwnerMock = this.dataMock.getUsers().get(1);
    Workspace workspaceMock = this.dataMock.getWorkspaces().get(0);
    Project projectMock1 = this.dataMock.getProjects().get(0);
    Project projectMock2 = this.dataMock.getProjects().get(1);
    Project projectMock3 = this.dataMock.getProjects().get(2);

    User workspaceOwner = this.generateUserByMock(workspaceOwnerMock);
    User projectsOwner = this.generateUserByMock(projectsOwnerMock);
    Workspace workspace = this.generateWorkspaceByMock(workspaceMock, workspaceOwner);
    Project project1 = this.generateProjectByMock(projectMock1, workspace, workspaceOwner);
    Project project2 = this.generateProjectByMock(projectMock2, workspace, projectsOwner);
    Project project3 = this.generateProjectByMock(projectMock3, workspace, projectsOwner);

    this.entityManager.persist(workspaceOwner);
    this.entityManager.persist(projectsOwner);
    this.entityManager.persist(workspace);
    this.entityManager.persist(project1);
    this.entityManager.persist(project2);
    this.entityManager.persist(project3);

    Sort sort = Sort.by(Sort.Direction.ASC, "priority");

    List<Project> workspaceProjects = this.projectRepository.findAllByWorkspaceId(workspace.getId(), sort);

    assertThat(workspaceProjects)
      .allSatisfy(project -> assertThat(project.getWorkspace().getId()).isEqualTo(workspace.getId()))
      .hasSize(3);
  }

  @Test
  @DisplayName("findAllByUserId - Should successfully return all user projects")
  void findAllByUserIdSuccess() {
    User projectsOwnerMock = this.dataMock.getUsers().get(1);
    User workspaceOwnerMock = this.dataMock.getUsers().get(0);
    Workspace workspaceMock = this.dataMock.getWorkspaces().get(0);
    Project projectMock1 = this.dataMock.getProjects().get(0);
    Project projectMock2 = this.dataMock.getProjects().get(1);
    Project projectMock3 = this.dataMock.getProjects().get(2);

    User projectsOwner = this.generateUserByMock(projectsOwnerMock);
    User workspaceOwner = this.generateUserByMock(workspaceOwnerMock);
    Workspace workspace = this.generateWorkspaceByMock(workspaceMock, workspaceOwner);
    Project project1 = this.generateProjectByMock(projectMock1, workspace, workspaceOwner);
    Project project2 = this.generateProjectByMock(projectMock2, workspace, projectsOwner);
    Project project3 = this.generateProjectByMock(projectMock3, workspace, projectsOwner);

    this.entityManager.persist(projectsOwner);
    this.entityManager.persist(workspaceOwner);
    this.entityManager.persist(workspace);
    this.entityManager.persist(project1);
    this.entityManager.persist(project2);
    this.entityManager.persist(project3);

    List<Project> userProjects = this.projectRepository.findAllByUserId(projectsOwner.getId());

    assertThat(userProjects)
      .allSatisfy(project -> assertThat(project.getOwner().getId()).isEqualTo(projectsOwner.getId()))
      .hasSize(2);
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
