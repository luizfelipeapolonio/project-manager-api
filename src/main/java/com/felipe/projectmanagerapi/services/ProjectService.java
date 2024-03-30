package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.ProjectCreateDTO;
import com.felipe.projectmanagerapi.dtos.ProjectUpdateDTO;
import com.felipe.projectmanagerapi.dtos.mappers.ProjectMapper;
import com.felipe.projectmanagerapi.exceptions.InvalidBudgetException;
import com.felipe.projectmanagerapi.exceptions.InvalidCostException;
import com.felipe.projectmanagerapi.exceptions.InvalidDateException;
import com.felipe.projectmanagerapi.exceptions.OutOfBudgetException;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.infra.security.AuthorizationService;
import com.felipe.projectmanagerapi.infra.security.UserPrincipal;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.models.Task;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.repositories.ProjectRepository;
import com.felipe.projectmanagerapi.utils.ConvertDateFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    newProject.setBudget(new BigDecimal(project.budget()).setScale(2, RoundingMode.FLOOR));
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
          BigDecimal newBudget = new BigDecimal(projectUpdate.budget()).setScale(2, RoundingMode.FLOOR);
          if(newBudget.compareTo(project.getCost()) < 0) {
            throw new InvalidBudgetException(
              "O novo orçamento é menor do que o custo atual do projeto. " +
              "Novo orçamento: R$ " + newBudget +
              " Custo atual: R$ " + project.getCost()
            );
          }
          project.setBudget(newBudget);
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

  public List<Project> getAllFromWorkspace(@NotNull String workspaceId, String sortDirection) {
    Workspace workspace = this.workspaceService.getById(workspaceId);
    Sort sort = this.sortingOrder(sortDirection);
    return this.projectRepository.findAllByWorkspaceId(workspace.getId(), sort);
  }

  public List<Project> getAllFromAuthenticatedUser() {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    return this.projectRepository.findAllByUserId(userPrincipal.getUser().getId());
  }

  public List<Project> getAllFromOwner(@NotNull String ownerId) {
    User projectsOwner = this.userService.getProfile(ownerId);
    return this.projectRepository.findAllByUserId(projectsOwner.getId());
  }

  public Project delete(@NotNull String projectId) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Project project = this.projectRepository.findById(projectId)
      .orElseThrow(() -> new RecordNotFoundException("Projeto de ID: '" + projectId + "' não encontrado"));

    Workspace workspace = this.workspaceService.getById(project.getWorkspace().getId());

    String authenticatedUserId = userPrincipal.getUser().getId();
    String projectOwnerId = project.getOwner().getId();
    String workspaceOwnerId = workspace.getOwner().getId();

    if(!authenticatedUserId.equals(workspaceOwnerId) && !authenticatedUserId.equals(projectOwnerId)) {
      throw new AccessDeniedException("Acesso negado: Você não tem permissão para remover este recurso");
    }

    this.projectRepository.deleteById(project.getId());
    return project;
  }

  public List<Project> deleteAllFromWorkspace(@NotNull String workspaceId) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Workspace workspace = this.workspaceService.getById(workspaceId);
    Sort sort = this.sortingOrder("asc");

    if(!userPrincipal.getUser().getId().equals(workspace.getOwner().getId())) {
      throw new AccessDeniedException("Acesso negado: Você não tem permissão para remover este recurso");
    }

    List<Project> projects = this.projectRepository.findAllByWorkspaceId(workspace.getId(), sort);
    this.projectRepository.deleteAll(projects);
    return projects;
  }

  public List<Project> deleteAllFromAuthenticatedUser() {
    List<Project> projects = this.getAllFromAuthenticatedUser();
    this.projectRepository.deleteAll(projects);
    return projects;
  }

  public List<Project> deleteAllFromOwner(@NotNull String ownerId) {
    List<Project> projects = this.getAllFromOwner(ownerId);
    this.projectRepository.deleteAll(projects);
    return projects;
  }

  public List<Project> deleteAllFromOwnerAndWorkspace(@NotNull String workspaceId, @NotNull String ownerId) {
    List<Project> projects = this.getAllByWorkspaceAndOwner(workspaceId, ownerId);
    this.projectRepository.deleteAll(projects);
    return projects;
  }

  public void addCost(Project project, BigDecimal newCost) {
    if(newCost.compareTo(project.getBudget()) > 0) {
      throw new OutOfBudgetException(project.getBudget(), newCost);
    }
    if(newCost.compareTo(BigDecimal.ZERO) < 0) {
      throw new InvalidCostException("Custo inválido! Valores negativos não são permitidos. Custo: R$ " + newCost);
    }
    project.setCost(project.getCost().add(newCost));
    this.projectRepository.save(project);
  }

  public void updateCost(Project project, Task task, BigDecimal newCost) {
    if(newCost.compareTo(project.getBudget()) > 0) {
      throw new OutOfBudgetException(project.getBudget(), newCost);
    }
    if(newCost.compareTo(BigDecimal.ZERO) < 0) {
      throw new InvalidCostException("Custo inválido! Valores negativos não são permitidos. Custo: R$ " + newCost);
    }
    BigDecimal oldCost = project.getCost().subtract(task.getCost());
    BigDecimal updatedCost = oldCost.add(newCost);
    project.setCost(updatedCost);
    this.projectRepository.save(project);
  }

  public void subtractCost(Project project, Task task) {
    project.setCost(project.getCost().subtract(task.getCost()));
    this.projectRepository.save(project);
  }

  public void resetCost(Project project) {
    project.setCost(BigDecimal.ZERO);
    this.projectRepository.save(project);
  }

  private Sort sortingOrder(String sortDirection) {
    return Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), "priority");
  }
}
