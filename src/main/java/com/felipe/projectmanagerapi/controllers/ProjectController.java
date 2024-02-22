package com.felipe.projectmanagerapi.controllers;

import com.felipe.projectmanagerapi.dtos.ProjectCreateDTO;
import com.felipe.projectmanagerapi.dtos.ProjectResponseDTO;
import com.felipe.projectmanagerapi.dtos.mappers.ProjectMapper;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.services.ProjectService;
import com.felipe.projectmanagerapi.utils.CustomResponseBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

  private final ProjectService projectService;
  private final ProjectMapper projectMapper;

  public ProjectController(ProjectService projectService, ProjectMapper projectMapper) {
    this.projectService = projectService;
    this.projectMapper = projectMapper;
  }

  @PostMapping("/test")
  public ProjectCreateDTO test(@RequestBody @Valid ProjectCreateDTO body) {
    return body;
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
}
