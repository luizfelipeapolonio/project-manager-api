package com.felipe.projectmanagerapi.controllers;

import com.felipe.projectmanagerapi.dtos.ProjectCreateDTO;
import com.felipe.projectmanagerapi.dtos.ProjectResponseDTO;
import com.felipe.projectmanagerapi.dtos.ProjectUpdateDTO;
import com.felipe.projectmanagerapi.dtos.mappers.ProjectMapper;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.services.ProjectService;
import com.felipe.projectmanagerapi.utils.CustomResponseBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

  private final ProjectService projectService;
  private final ProjectMapper projectMapper;

  public ProjectController(ProjectService projectService, ProjectMapper projectMapper) {
    this.projectService = projectService;
    this.projectMapper = projectMapper;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CustomResponseBody<ProjectResponseDTO> create(@RequestBody @NotNull @Valid ProjectCreateDTO project) {
    Project createdProject = this.projectService.create(project);
    ProjectResponseDTO createdProjectResponseDTO = this.projectMapper.toDTO(createdProject);

    CustomResponseBody<ProjectResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.CREATED);
    response.setMessage("Projeto criado com sucesso");
    response.setData(createdProjectResponseDTO);
    return response;
  }

  @PatchMapping("/{projectId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<ProjectResponseDTO> update(
    @PathVariable @NotNull @NotBlank String projectId,
    @RequestBody @NotNull @Valid ProjectUpdateDTO project
  ) {
    Project updatedProject = this.projectService.update(projectId, project);
    ProjectResponseDTO projectResponseDTO = this.projectMapper.toDTO(updatedProject);

    CustomResponseBody<ProjectResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Projeto atualizado com sucesso");
    response.setData(projectResponseDTO);
    return response;
  }

  @RequestMapping("/{projectId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<ProjectResponseDTO> getById(@PathVariable @NotNull @NotBlank String projectId) {
    Project project = this.projectService.getById(projectId);
    ProjectResponseDTO projectResponseDTO = this.projectMapper.toDTO(project);

    CustomResponseBody<ProjectResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Projeto encontrado");
    response.setData(projectResponseDTO);
    return response;
  }

  @RequestMapping("/workspaces/{workspaceId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<List<ProjectResponseDTO>> getAllFromWorkspace(@PathVariable @NotNull @NotBlank String workspaceId) {
    List<Project> projects = this.projectService.getAllFromWorkspace(workspaceId);
    List<ProjectResponseDTO> projectsDTO = projects.stream().map(this.projectMapper::toDTO).toList();

    CustomResponseBody<List<ProjectResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os projetos do workspace de ID: '" + workspaceId + "'");
    response.setData(projectsDTO);
    return response;
  }

  @RequestMapping("/workspaces/{workspaceId}/owner/{ownerId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<List<ProjectResponseDTO>> getAllByWorkspaceAndOwner(
    @PathVariable @NotNull @NotBlank String workspaceId,
    @PathVariable @NotNull @NotBlank String ownerId
  ) {
    List<Project> projects = this.projectService.getAllByWorkspaceAndOwner(workspaceId, ownerId);
    List<ProjectResponseDTO> projectDTOs = projects.stream().map(this.projectMapper::toDTO).toList();

    CustomResponseBody<List<ProjectResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os projetos do usuário de id '" + ownerId + "' no workspace de id '" + workspaceId + "'");
    response.setData(projectDTOs);
    return response;
  }
}
