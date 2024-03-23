package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.TaskCreateDTO;
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
import java.util.Optional;

@Service
public class TaskService {

  private final TaskRepository taskRepository;
  private final AuthorizationService authorizationService;
  private final ProjectService projectService;

  public TaskService(
    TaskRepository taskRepository,
    AuthorizationService authorizationService,
    ProjectService projectService
  ) {
    this.taskRepository = taskRepository;
    this.authorizationService = authorizationService;
    this.projectService = projectService;
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
    // TODO: Deve ser o dono do workspace, do projeto ou da task
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Task task = this.taskRepository.findById(taskId)
      .orElseThrow(() -> new RecordNotFoundException("Task de ID: '" + taskId + "' não encontrada"));

    Project project = task.getProject();
    Workspace workspace = project.getWorkspace();
    String workspaceOwnerId = workspace.getOwner().getId();
    String projectOwnerId = project.getOwner().getId();
    String authenticatedUserId = userPrincipal.getUser().getId();
    String taskOwnerId = task.getOwner().getId();

    Optional<User> existingMember = workspace.getMembers()
      .stream()
      .filter(member -> member.getId().equals(authenticatedUserId))
      .findFirst();

    if(!workspaceOwnerId.equals(authenticatedUserId) && existingMember.isEmpty() ||
       !workspaceOwnerId.equals(authenticatedUserId) && !projectOwnerId.equals(authenticatedUserId) &&
       !taskOwnerId.equals(authenticatedUserId)
    ) {
      throw new AccessDeniedException("Acesso negado: Você não tem permissão para remover este recurso");
    }

    this.projectService.subtractCost(project, task);
    this.taskRepository.deleteById(task.getId());
    return task;
  }
}
