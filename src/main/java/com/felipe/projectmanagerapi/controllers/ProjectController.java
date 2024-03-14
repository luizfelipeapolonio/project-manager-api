package com.felipe.projectmanagerapi.controllers;

import com.felipe.projectmanagerapi.dtos.ProjectCreateDTO;
import com.felipe.projectmanagerapi.dtos.ProjectFullResponseDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    ProjectResponseDTO createdProjectResponseDTO = this.projectMapper.toProjectResponseDTO(createdProject);

    CustomResponseBody<ProjectResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.CREATED);
    response.setMessage("Projeto criado com sucesso");
    response.setData(createdProjectResponseDTO);
    return response;
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<List<ProjectResponseDTO>> getAllFromAuthenticatedUser() {
    List<Project> projects = this.projectService.getAllFromAuthenticatedUser();
    List<ProjectResponseDTO> projectsDTO = projects.stream().map(this.projectMapper::toProjectResponseDTO).toList();

    CustomResponseBody<List<ProjectResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os seus projetos");
    response.setData(projectsDTO);
    return response;
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Map<String, List<ProjectResponseDTO>>> deleteAllFromAuthenticatedUser() {
    List<Project> deletedProjects = this.projectService.deleteAllFromAuthenticatedUser();
    List<ProjectResponseDTO> deletedProjectsDTO = deletedProjects.stream()
      .map(this.projectMapper::toProjectResponseDTO)
      .toList();

    Map<String, List<ProjectResponseDTO>> deletedProjectsMap = new HashMap<>(1);
    deletedProjectsMap.put("deletedProjects", deletedProjectsDTO);

    CustomResponseBody<Map<String, List<ProjectResponseDTO>>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os seus projetos foram excluídos com sucesso");
    response.setData(deletedProjectsMap);
    return response;
  }

  @PatchMapping("/{projectId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<ProjectResponseDTO> update(
    @PathVariable @NotNull @NotBlank String projectId,
    @RequestBody @NotNull @Valid ProjectUpdateDTO project
  ) {
    Project updatedProject = this.projectService.update(projectId, project);
    ProjectResponseDTO projectResponseDTO = this.projectMapper.toProjectResponseDTO(updatedProject);

    CustomResponseBody<ProjectResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Projeto atualizado com sucesso");
    response.setData(projectResponseDTO);
    return response;
  }

  @GetMapping("/{projectId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<ProjectFullResponseDTO> getById(@PathVariable @NotNull @NotBlank String projectId) {
    Project project = this.projectService.getById(projectId);
    ProjectFullResponseDTO projectResponseDTO = this.projectMapper.toProjectFullResponseDTO(project);

    CustomResponseBody<ProjectFullResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Projeto encontrado");
    response.setData(projectResponseDTO);
    return response;
  }

  @DeleteMapping("/{projectId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Map<String, ProjectResponseDTO>> delete(@PathVariable @NotNull @NotBlank String projectId) {
    Project deletedProject = this.projectService.delete(projectId);
    ProjectResponseDTO projectResponseDTO = this.projectMapper.toProjectResponseDTO(deletedProject);

    Map<String, ProjectResponseDTO> deletedProjectMap = new HashMap<>(1);
    deletedProjectMap.put("deletedProject", projectResponseDTO);

    CustomResponseBody<Map<String, ProjectResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Projeto excluído com sucesso");
    response.setData(deletedProjectMap);
    return response;
  }

  @DeleteMapping("/workspaces/{workspaceId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Map<String, List<ProjectResponseDTO>>> deleteAllFromWorkspace(
    @PathVariable @NotNull @NotBlank String workspaceId
  ) {
    List<Project> deletedProjects = this.projectService.deleteAllFromWorkspace(workspaceId);
    List<ProjectResponseDTO> deletedProjectsDTO = deletedProjects.stream()
      .map(this.projectMapper::toProjectResponseDTO)
      .toList();

    Map<String, List<ProjectResponseDTO>> deletedProjectsMap = new HashMap<>(1);
    deletedProjectsMap.put("deletedProjects", deletedProjectsDTO);

    CustomResponseBody<Map<String, List<ProjectResponseDTO>>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os projetos do workspace de ID: '" + workspaceId + "' excluídos com sucesso");
    response.setData(deletedProjectsMap);
    return response;
  }

  @GetMapping("/workspaces/{workspaceId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<List<ProjectResponseDTO>> getAllFromWorkspace(@PathVariable @NotNull @NotBlank String workspaceId) {
    List<Project> projects = this.projectService.getAllFromWorkspace(workspaceId);
    List<ProjectResponseDTO> projectsDTO = projects.stream().map(this.projectMapper::toProjectResponseDTO).toList();

    CustomResponseBody<List<ProjectResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os projetos do workspace de ID: '" + workspaceId + "'");
    response.setData(projectsDTO);
    return response;
  }

  @GetMapping("/workspaces/{workspaceId}/owner/{ownerId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<List<ProjectResponseDTO>> getAllByWorkspaceAndOwner(
    @PathVariable @NotNull @NotBlank String workspaceId,
    @PathVariable @NotNull @NotBlank String ownerId
  ) {
    List<Project> projects = this.projectService.getAllByWorkspaceAndOwner(workspaceId, ownerId);
    List<ProjectResponseDTO> projectDTOs = projects.stream().map(this.projectMapper::toProjectResponseDTO).toList();

    CustomResponseBody<List<ProjectResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os projetos do usuário de id '" + ownerId + "' no workspace de id '" + workspaceId + "'");
    response.setData(projectDTOs);
    return response;
  }

  @DeleteMapping("/workspaces/{workspaceId}/owner/{ownerId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Map<String, List<ProjectResponseDTO>>> deleteAllFromOwnerAndWorkspace(
    @PathVariable @NotNull @NotBlank String workspaceId,
    @PathVariable @NotNull @NotBlank String ownerId
  ) {
    List<Project> deletedProjects = this.projectService.deleteAllFromOwnerAndWorkspace(workspaceId, ownerId);
    List<ProjectResponseDTO> deletedProjectsDTO = deletedProjects.stream()
      .map(this.projectMapper::toProjectResponseDTO)
      .toList();

    Map<String, List<ProjectResponseDTO>> deletedProjectsMap = new HashMap<>(1);
    deletedProjectsMap.put("deletedProjects", deletedProjectsDTO);

    CustomResponseBody<Map<String, List<ProjectResponseDTO>>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage(
      "Todos os projetos do usuário de ID '" + ownerId +
      "' do workspace de ID '" + workspaceId + "' foram excluídos com sucesso"
    );
    response.setData(deletedProjectsMap);
    return response;
  }
}
