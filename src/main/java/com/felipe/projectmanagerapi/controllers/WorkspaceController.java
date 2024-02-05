package com.felipe.projectmanagerapi.controllers;

import com.felipe.projectmanagerapi.dtos.WorkspaceCreateOrUpdateDTO;
import com.felipe.projectmanagerapi.dtos.WorkspaceResponseDTO;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.services.WorkspaceService;
import com.felipe.projectmanagerapi.utils.CustomResponseBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

  private final WorkspaceService workspaceService;

  public WorkspaceController(WorkspaceService workspaceService) {
    this.workspaceService = workspaceService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CustomResponseBody<WorkspaceResponseDTO> create(@RequestBody @Valid @NotNull WorkspaceCreateOrUpdateDTO body) {
    WorkspaceResponseDTO createdWorkspace = this.workspaceService.create(body);

    CustomResponseBody<WorkspaceResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.CREATED);
    response.setMessage("Workspace criado com sucesso");
    response.setData(createdWorkspace);
    return response;
  }

  @PatchMapping("/{workspaceId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<WorkspaceResponseDTO> update(
    @PathVariable @NotNull @NotBlank String workspaceId,
    @RequestBody @Valid @NotNull WorkspaceCreateOrUpdateDTO body
  ) {
    WorkspaceResponseDTO updatedWorkspace = this.workspaceService.update(workspaceId, body);

    CustomResponseBody<WorkspaceResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Workspace atualizado com sucesso");
    response.setData(updatedWorkspace);
    return response;
  }
}
