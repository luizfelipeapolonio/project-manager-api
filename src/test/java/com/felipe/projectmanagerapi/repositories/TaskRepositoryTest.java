package com.felipe.projectmanagerapi.repositories;

import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.models.Task;
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
public class TaskRepositoryTest {

  @Autowired
  EntityManager entityManager;

  @Autowired
  TaskRepository taskRepository;

  private final GenerateMocks dataMock = new GenerateMocks();

  @Test
  @DisplayName("getAllFromProject - Should successfully return all tasks from a specific project")
  void getAllFromProjectSuccess() {
    User workspaceOwnerMock = this.dataMock.getUsers().get(0);
    User projectOwnerMock = this.dataMock.getUsers().get(1);
    User tasksOwnerMock = this.dataMock.getUsers().get(2);
    Workspace workspaceMock = this.dataMock.getWorkspaces().get(0);
    Project projectMock = this.dataMock.getProjects().get(1);
    Task taskMock1 = this.dataMock.getTasks().get(0);
    Task taskMock2 = this.dataMock.getTasks().get(1);

    User workspaceOwner = this.generateUserByMock(workspaceOwnerMock);
    User projectOwner = this.generateUserByMock(projectOwnerMock);
    User tasksOwner = this.generateUserByMock(tasksOwnerMock);
    Workspace workspace = this.generateWorkspaceByMock(workspaceMock, workspaceOwner);
    Project project = this.generateProjectByMock(projectMock, workspace, projectOwner);
    Task task1 = this.generateTaskByMock(taskMock1, project, tasksOwner);
    Task task2 = this.generateTaskByMock(taskMock2, project, tasksOwner);

    this.entityManager.persist(workspaceOwner);
    this.entityManager.persist(projectOwner);
    this.entityManager.persist(tasksOwner);
    this.entityManager.persist(workspace);
    this.entityManager.persist(project);
    this.entityManager.persist(task1);
    this.entityManager.persist(task2);

    List<Task> allTasksFromProject = this.taskRepository.findAllByProjectId(project.getId());

    assertThat(allTasksFromProject)
      .allSatisfy(task -> assertThat(task.getProject().getId()).isEqualTo(project.getId()))
      .hasSize(2);
  }

  @Test
  @DisplayName("findAllByOwnerId - Should successfully return all authenticated user tasks")
  void findAllByOwnerIdSuccess() {
    User workspaceOwnerMock = this.dataMock.getUsers().get(0);
    User projectAndTaskOwnerMock = this.dataMock.getUsers().get(1);
    Workspace workspaceMock = this.dataMock.getWorkspaces().get(0);
    Project projectMock = this.dataMock.getProjects().get(1);
    Task taskMock1 = this.dataMock.getTasks().get(0);
    Task taskMock2 = this.dataMock.getTasks().get(1);

    User workspaceOwner = this.generateUserByMock(workspaceOwnerMock);
    User projectAndTaskOwner = this.generateUserByMock(projectAndTaskOwnerMock);
    Workspace workspace = this.generateWorkspaceByMock(workspaceMock, workspaceOwner);
    Project project = this.generateProjectByMock(projectMock, workspace, projectAndTaskOwner);
    Task task1 = this.generateTaskByMock(taskMock1, project, projectAndTaskOwner);
    Task task2 = this.generateTaskByMock(taskMock2, project, projectAndTaskOwner);

    this.entityManager.persist(workspaceOwner);
    this.entityManager.persist(projectAndTaskOwner);
    this.entityManager.persist(projectAndTaskOwner);
    this.entityManager.persist(workspace);
    this.entityManager.persist(project);
    this.entityManager.persist(task1);
    this.entityManager.persist(task2);

    List<Task> allTasks = this.taskRepository.findAllByOwnerId(projectAndTaskOwner.getId());

    assertThat(allTasks)
      .allSatisfy(task -> assertThat(task.getOwner().getId()).isEqualTo(projectAndTaskOwner.getId()))
      .hasSize(2);
  }

  private Task generateTaskByMock(Task task, Project project, User owner) {
    Task newTask = new Task();
    newTask.setName(task.getName());
    newTask.setDescription(task.getDescription());
    newTask.setCost(task.getCost());
    newTask.setCreatedAt(task.getCreatedAt());
    newTask.setUpdatedAt(task.getUpdatedAt());
    newTask.setProject(project);
    newTask.setOwner(owner);
    return newTask;
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
