package com.felipe.projectmanagerapi.controllers;

import com.felipe.projectmanagerapi.dtos.*;
import com.felipe.projectmanagerapi.dtos.mappers.UserMapper;
import com.felipe.projectmanagerapi.dtos.mappers.WorkspaceMapper;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.services.WorkspaceService;
import com.felipe.projectmanagerapi.utils.CustomResponseBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

  private final WorkspaceService workspaceService;
  private final WorkspaceMapper workspaceMapper;
  private final UserMapper userMapper;

  public WorkspaceController(WorkspaceService workspaceService, WorkspaceMapper workspaceMapper, UserMapper userMapper) {
    this.workspaceService = workspaceService;
    this.workspaceMapper = workspaceMapper;
    this.userMapper = userMapper;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CustomResponseBody<WorkspaceResponseDTO> create(@RequestBody @Valid @NotNull WorkspaceCreateOrUpdateDTO body) {
    Workspace createdWorkspace = this.workspaceService.create(body);
    WorkspaceResponseDTO createdWorkspaceDTO = this.workspaceMapper.toWorkspaceResponseDTO(createdWorkspace);

    CustomResponseBody<WorkspaceResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.CREATED);
    response.setMessage("Workspace criado com sucesso");
    response.setData(createdWorkspaceDTO);
    return response;
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<List<WorkspaceResponseDTO>> getAllUserWorkspaces() {
    List<Workspace> workspaces = this.workspaceService.getAllUserWorkspaces();
    List<WorkspaceResponseDTO> workspacesDTO = workspaces.stream()
      .map(this.workspaceMapper::toWorkspaceResponseDTO)
      .toList();

    CustomResponseBody<List<WorkspaceResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os seus workspaces");
    response.setData(workspacesDTO);
    return response;
  }

  @PatchMapping("/{workspaceId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<WorkspaceResponseDTO> update(
    @PathVariable @NotNull @NotBlank String workspaceId,
    @RequestBody @Valid @NotNull WorkspaceCreateOrUpdateDTO body
  ) {
    Workspace updatedWorkspace = this.workspaceService.update(workspaceId, body);
    WorkspaceResponseDTO updatedWorkspaceDTO = this.workspaceMapper.toWorkspaceResponseDTO(updatedWorkspace);

    CustomResponseBody<WorkspaceResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Workspace atualizado com sucesso");
    response.setData(updatedWorkspaceDTO);
    return response;
  }

  @GetMapping("/{workspaceId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<WorkspaceFullResponseDTO> getById(@PathVariable @NotNull @NotBlank String workspaceId) {
    Workspace workspace = this.workspaceService.getById(workspaceId);
    WorkspaceFullResponseDTO workspaceFullResponseDTO = this.workspaceMapper.toWorkspaceFullResponseDTO(workspace);

    CustomResponseBody<WorkspaceFullResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Workspace encontrado");
    response.setData(workspaceFullResponseDTO);
    return response;
  }

  @DeleteMapping("/{workspaceId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Map<String, WorkspaceResponseDTO>> delete(@PathVariable @NotNull @NotBlank String workspaceId) {
    Workspace deletedWorkspace = this.workspaceService.delete(workspaceId);
    WorkspaceResponseDTO deletedWorkspaceDTO = this.workspaceMapper.toWorkspaceResponseDTO(deletedWorkspace);

    Map<String, WorkspaceResponseDTO> deletedWorkspaceMap = new HashMap<>();
    deletedWorkspaceMap.put("deletedWorkspace", deletedWorkspaceDTO);

    CustomResponseBody<Map<String, WorkspaceResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Workspace deletado com sucesso");
    response.setData(deletedWorkspaceMap);
    return response;
  }

  @GetMapping("/{workspaceId}/members")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<WorkspaceMembersResponseDTO> getAllMembers(@PathVariable @NotNull @NotBlank String workspaceId) {
    Workspace workspace = this.workspaceService.getById(workspaceId);
    WorkspaceResponseDTO workspaceDTO = this.workspaceMapper.toWorkspaceResponseDTO(workspace);
    List<UserResponseDTO> members = workspace.getMembers().stream().map(this.userMapper::toDTO).toList();
    WorkspaceMembersResponseDTO workspaceMembersDTO = new WorkspaceMembersResponseDTO(workspaceDTO, members);

    CustomResponseBody<WorkspaceMembersResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os membros do workspace");
    response.setData(workspaceMembersDTO);
    return response;
  }

  @PatchMapping("/{workspaceId}/members/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<WorkspaceMembersResponseDTO> insertMember(
    @PathVariable @NotNull @NotBlank String workspaceId,
    @PathVariable @NotNull @NotBlank String userId
  ) {
    Workspace workspace = this.workspaceService.insertMember(workspaceId, userId);
    WorkspaceResponseDTO workspaceDTO = this.workspaceMapper.toWorkspaceResponseDTO(workspace);
    List<UserResponseDTO> members = workspace.getMembers().stream().map(this.userMapper::toDTO).toList();
    WorkspaceMembersResponseDTO workspaceMembersDTO = new WorkspaceMembersResponseDTO(workspaceDTO, members);

    CustomResponseBody<WorkspaceMembersResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Membro inserido no workspace com sucesso");
    response.setData(workspaceMembersDTO);
    return response;
  }

  @DeleteMapping("/{workspaceId}/members/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<WorkspaceMembersResponseDTO> removeMember(
    @PathVariable @NotNull @NotBlank String workspaceId,
    @PathVariable @NotNull @NotBlank String userId
  ) {
    Workspace workspace = this.workspaceService.removeMember(workspaceId, userId);
    WorkspaceResponseDTO workspaceDTO = this.workspaceMapper.toWorkspaceResponseDTO(workspace);
    List<UserResponseDTO> members = workspace.getMembers().stream().map(this.userMapper::toDTO).toList();
    WorkspaceMembersResponseDTO workspaceMembersDTO = new WorkspaceMembersResponseDTO(workspaceDTO, members);

    CustomResponseBody<WorkspaceMembersResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Membro removido do workspace com sucesso");
    response.setData(workspaceMembersDTO);
    return response;
  }
}
