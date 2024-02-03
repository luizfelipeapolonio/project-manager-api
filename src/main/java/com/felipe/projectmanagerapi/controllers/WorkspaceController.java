package com.felipe.projectmanagerapi.controllers;

import com.felipe.projectmanagerapi.dtos.WorkspaceCreateDTO;
import com.felipe.projectmanagerapi.dtos.WorkspaceResponseDTO;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.services.WorkspaceService;
import com.felipe.projectmanagerapi.utils.CustomResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

  private final WorkspaceService workspaceService;

  public WorkspaceController(WorkspaceService workspaceService) {
    this.workspaceService = workspaceService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CustomResponseBody<WorkspaceResponseDTO> create(@RequestBody WorkspaceCreateDTO body) {
    WorkspaceResponseDTO createdWorkspace = this.workspaceService.create(body);

    CustomResponseBody<WorkspaceResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.CREATED);
    response.setMessage("Workspace criado com sucesso");
    response.setData(createdWorkspace);
    return response;
  }
}
