package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.ProjectCreateDTO;
import com.felipe.projectmanagerapi.dtos.ProjectUpdateDTO;
import com.felipe.projectmanagerapi.dtos.mappers.ProjectMapper;
import com.felipe.projectmanagerapi.exceptions.InvalidDateException;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.infra.security.AuthorizationService;
import com.felipe.projectmanagerapi.infra.security.UserPrincipal;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.repositories.ProjectRepository;
import com.felipe.projectmanagerapi.utils.ConvertDateFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final AuthorizationService authorizationService;
  private final WorkspaceService workspaceService;
  private final UserService userService;
  private final ProjectMapper projectMapper;

  public ProjectService(
    ProjectRepository projectRepository,
    AuthorizationService authorizationService,
    WorkspaceService workspaceService,
    UserService userService,
    ProjectMapper projectMapper
  ) {
    this.projectRepository = projectRepository;
    this.authorizationService = authorizationService;
    this.workspaceService = workspaceService;
    this.userService = userService;
    this.projectMapper = projectMapper;
  }

  public Project create(@NotNull @Valid ProjectCreateDTO project) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Workspace currentWorkspace = this.workspaceService.getById(project.workspaceId());

    LocalDate today = LocalDate.now();
    LocalDate projectDeadline = ConvertDateFormat.convertFormattedStringToDate(project.deadline());

    if(projectDeadline.isBefore(today)) {
      throw new InvalidDateException(
        "Data inválida. O prazo de entrega do projeto não deve ser antes da data atual" +
        "\nData atual: " + today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) +
        "\nPrazo do projeto: " + ConvertDateFormat.convertDateToFormattedString(projectDeadline)
      );
    }

    Project newProject = new Project();
    newProject.setName(project.name());
    newProject.setCategory(project.category());
    newProject.setDescription(project.description());
    newProject.setPriority(this.projectMapper.convertValueToPriorityLevel(project.priority()));
    newProject.setBudget(project.budget());
    newProject.setDeadline(projectDeadline);
    newProject.setOwner(userPrincipal.getUser());
    newProject.setWorkspace(currentWorkspace);

    return this.projectRepository.save(newProject);
  }

  public Project update(@NotNull String projectId, @NotNull @Valid ProjectUpdateDTO projectUpdate) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    LocalDate today = LocalDate.now();

    return this.projectRepository.findById(projectId)
      .map(project -> {
        if(!project.getOwner().getId().equals(userPrincipal.getUser().getId())) {
          throw new AccessDeniedException("Acesso negado: Você não tem permissão para alterar este recurso");
        }

        if(projectUpdate.name() != null) {
          project.setName(projectUpdate.name());
        }
        if(projectUpdate.category() != null) {
          project.setCategory(projectUpdate.category());
        }
        if(projectUpdate.description() != null) {
          project.setDescription(projectUpdate.description());
        }
        if(projectUpdate.budget() != null) {
          project.setBudget(projectUpdate.budget());
        }
        if(projectUpdate.priority() != null) {
          project.setPriority(this.projectMapper.convertValueToPriorityLevel(projectUpdate.priority()));
        }
        if(projectUpdate.deadline() != null) {
          LocalDate deadlineToUpdateDate = ConvertDateFormat.convertFormattedStringToDate(projectUpdate.deadline());
          if(deadlineToUpdateDate.isBefore(today)) {
            throw new InvalidDateException(
              "Data inválida. O prazo de entrega do projeto não deve ser antes da data atual" +
              "\nData atual: " + today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) +
              "\nPrazo do projeto: " + projectUpdate.deadline()
            );
          }
          project.setDeadline(ConvertDateFormat.convertFormattedStringToDate(projectUpdate.deadline()));
        }
        return this.projectRepository.save(project);
      })
      .orElseThrow(() -> new RecordNotFoundException("Projeto de ID: '" + projectId + "' não encontrado"));
  }

  public List<Project> getAllByWorkspaceAndOwner(@NotNull String workspaceId, @NotNull String ownerId) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Workspace workspace = this.workspaceService.getById(workspaceId);
    User projectsOwner = this.userService.getProfile(ownerId);

    if(!workspace.getOwner().getId().equals(userPrincipal.getUser().getId())) {
      throw new AccessDeniedException("Acesso negado: Você não tem permissão para acessar este recurso");
    }

    return this.projectRepository.findAllByWorkspaceIdAndOwnerId(workspace.getId(), projectsOwner.getId());
  }

  public Project getById(@NotNull String projectId) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Project project = this.projectRepository.findById(projectId)
      .orElseThrow(() -> new RecordNotFoundException("Projeto de ID: '" + projectId + "' não encontrado"));

    String authenticatedUserId = userPrincipal.getUser().getId();
    String workspaceOwnerId = project.getWorkspace().getOwner().getId();

    Optional<User> existingMember = project.getWorkspace().getMembers()
      .stream()
      .filter(member -> member.getId().equals(authenticatedUserId))
      .findFirst();

    if(!workspaceOwnerId.equals(authenticatedUserId) && existingMember.isEmpty()) {
      throw new AccessDeniedException("Acesso negado: Você não tem permissão para acessar este recurso");
    }

    return project;
  }

  public List<Project> getAllFromWorkspace(@NotNull String workspaceId) {
    Workspace workspace = this.workspaceService.getById(workspaceId);
    return this.projectRepository.findAllByWorkspaceId(workspace.getId());
  }

  public List<Project> getAllFromAuthenticatedUser() {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    return this.projectRepository.findAllByUserId(userPrincipal.getUser().getId());
  }

  public void deleteAllFromOwnerAndWorkspace(@NotNull String workspaceId, @NotNull String ownerId) {
    List<Project> projects = this.getAllByWorkspaceAndOwner(workspaceId, ownerId);
    this.projectRepository.deleteAll(projects);
  }
}
