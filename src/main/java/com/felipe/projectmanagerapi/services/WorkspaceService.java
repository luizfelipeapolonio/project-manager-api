package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.WorkspaceCreateDTO;
import com.felipe.projectmanagerapi.dtos.WorkspaceResponseDTO;
import com.felipe.projectmanagerapi.dtos.mappers.WorkspaceMapper;
import com.felipe.projectmanagerapi.infra.security.AuthorizationService;
import com.felipe.projectmanagerapi.infra.security.UserPrincipal;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.repositories.WorkspaceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkspaceService {

  private final WorkspaceRepository workspaceRepository;
  private final AuthorizationService authorizationService;
  private final WorkspaceMapper workspaceMapper;

  public WorkspaceService(WorkspaceRepository workspaceRepository, AuthorizationService authorizationService, WorkspaceMapper workspaceMapper) {
    this.workspaceRepository = workspaceRepository;
    this.authorizationService = authorizationService;
    this.workspaceMapper = workspaceMapper;
  }

  public WorkspaceResponseDTO create(WorkspaceCreateDTO workspaceDTO) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Workspace workspace = new Workspace();
    workspace.setName(workspaceDTO.name());
    workspace.setOwner(userPrincipal.getUser());
    workspace.setMembers(List.of(userPrincipal.getUser()));

    Workspace createdWorkspace = this.workspaceRepository.save(workspace);
    return this.workspaceMapper.toDTO(createdWorkspace);
  }
}