package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.TaskCreateDTO;
import com.felipe.projectmanagerapi.dtos.TaskUpdateDTO;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.infra.security.AuthorizationService;
import com.felipe.projectmanagerapi.infra.security.UserPrincipal;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.models.Task;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.repositories.TaskRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

  private final TaskRepository taskRepository;
  private final AuthorizationService authorizationService;
  private final ProjectService projectService;
  private final UserService userService;

  public TaskService(
    TaskRepository taskRepository,
    AuthorizationService authorizationService,
    ProjectService projectService,
    UserService userService
  ) {
    this.taskRepository = taskRepository;
    this.authorizationService = authorizationService;
    this.projectService = projectService;
    this.userService = userService;
  }

  public Task create(@NotNull @Valid TaskCreateDTO task) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Project project = this.projectService.getById(task.projectId());
    BigDecimal cost = new BigDecimal(task.cost()).setScale(2, RoundingMode.FLOOR);

    Task newTask = new Task();
    newTask.setName(task.name());
    newTask.setDescription(task.description());
    newTask.setCost(cost);
    newTask.setProject(project);
    newTask.setOwner(userPrincipal.getUser());

    this.projectService.addCost(project, newTask.getCost());
    return this.taskRepository.save(newTask);
  }

  public Task getById(@NotNull String taskId) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    return this.taskRepository.findById(taskId)
      .map(task -> {
        Workspace workspace = task.getProject().getWorkspace();
        String authenticatedUserId = userPrincipal.getUser().getId();
        String workspaceOwnerId = workspace.getOwner().getId();

        Optional<User> existingMember = workspace.getMembers()
          .stream()
          .filter(member -> member.getId().equals(authenticatedUserId))
          .findFirst();

        if(!workspaceOwnerId.equals(authenticatedUserId) && existingMember.isEmpty()) {
          throw new AccessDeniedException("Acesso negado: Você não tem permissão para acessar este recurso");
        }

        return task;
      })
      .orElseThrow(() -> new RecordNotFoundException("Task de ID: '" + taskId + "' não encontrada"));
  }

  public Task delete(@NotNull String taskId) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Task task = this.taskRepository.findById(taskId)
      .orElseThrow(() -> new RecordNotFoundException("Task de ID: '" + taskId + "' não encontrada"));

    Project project = task.getProject();

    if(this.isNotAllowed(task, userPrincipal)) {
      throw new AccessDeniedException("Acesso negado: Você não tem permissão para remover este recurso");
    }

    this.projectService.subtractCost(project, task);
    this.taskRepository.deleteById(task.getId());
    return task;
  }

  public Task update(@NotNull String taskId, @NotNull @Valid TaskUpdateDTO taskUpdateDTO) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    return this.taskRepository.findById(taskId)
      .map(task -> {
        if(this.isNotAllowed(task, userPrincipal)) {
          throw new AccessDeniedException("Acesso negado: Você não tem permissão para atualizar este recurso");
        }

        if(taskUpdateDTO.name() != null) {
          task.setName(taskUpdateDTO.name());
        }
        if(taskUpdateDTO.description() != null) {
          task.setDescription(taskUpdateDTO.description());
        }
        if(taskUpdateDTO.cost() != null) {
          BigDecimal newCost = new BigDecimal(taskUpdateDTO.cost()).setScale(2, RoundingMode.FLOOR);
          this.projectService.updateCost(task.getProject(), task, newCost);
          task.setCost(newCost);
        }
        return this.taskRepository.save(task);
      })
      .orElseThrow(() -> new RecordNotFoundException("Task de ID: '" + taskId + "' não encontrada"));
  }

  public List<Task> getAllFromProject(@NotNull String projectId) {
    Project project = this.projectService.getById(projectId);
    return this.taskRepository.findAllByProjectId(project.getId());
  }

  public List<Task> getAllFromAuthenticatedUser() {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    return this.taskRepository.findAllByOwnerId(userPrincipal.getUser().getId());
  }

  public List<Task> getAllFromOwner(@NotNull String ownerId) {
    User tasksOwner = this.userService.getProfile(ownerId);
    return this.taskRepository.findAllByOwnerId(tasksOwner.getId());
  }

  public List<Task> deleteAllFromProject(@NotNull String projectId) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Project project = this.projectService.getById(projectId);
    String projectOwnerId = project.getOwner().getId();
    String workspaceOwnerId = project.getWorkspace().getOwner().getId();
    String authenticatedUserId = userPrincipal.getUser().getId();

    if(!workspaceOwnerId.equals(authenticatedUserId) && !projectOwnerId.equals(authenticatedUserId)) {
      throw new AccessDeniedException("Acesso negado: Você não tem permissão para remover estes recursos");
    }

    List<Task> tasks = this.taskRepository.findAllByProjectId(project.getId());
    this.taskRepository.deleteAll(tasks);
    this.projectService.resetCost(project);
    return tasks;
  }

  private boolean isNotAllowed(Task task, UserPrincipal authenticatedUser) {
    Project project = task.getProject();
    Workspace workspace = project.getWorkspace();
    String workspaceOwnerId = workspace.getOwner().getId();
    String projectOwnerId = project.getOwner().getId();
    String authenticatedUserId = authenticatedUser.getUser().getId();
    String taskOwnerId = task.getOwner().getId();

    Optional<User> existingMember = workspace.getMembers()
      .stream()
      .filter(member -> member.getId().equals(authenticatedUserId))
      .findFirst();

    if(!workspaceOwnerId.equals(authenticatedUserId) && existingMember.isEmpty() ||
       !workspaceOwnerId.equals(authenticatedUserId) && !projectOwnerId.equals(authenticatedUserId) &&
       !taskOwnerId.equals(authenticatedUserId)
    ) {
      return true;
    }
    return false;
  }
}
