package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.ProjectCreateDTO;
import com.felipe.projectmanagerapi.dtos.mappers.ProjectMapper;
import com.felipe.projectmanagerapi.exceptions.InvalidDateException;
import com.felipe.projectmanagerapi.infra.security.AuthorizationService;
import com.felipe.projectmanagerapi.infra.security.UserPrincipal;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.repositories.ProjectRepository;
import com.felipe.projectmanagerapi.utils.ConvertDateFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final AuthorizationService authorizationService;
  private final WorkspaceService workspaceService;
  private final ProjectMapper projectMapper;

  public ProjectService(
    ProjectRepository projectRepository,
    AuthorizationService authorizationService,
    WorkspaceService workspaceService,
    ProjectMapper projectMapper
  ) {
    this.projectRepository = projectRepository;
    this.authorizationService = authorizationService;
    this.workspaceService = workspaceService;
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
}
