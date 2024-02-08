package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.WorkspaceCreateOrUpdateDTO;
import com.felipe.projectmanagerapi.dtos.WorkspaceResponseDTO;
import com.felipe.projectmanagerapi.dtos.mappers.WorkspaceMapper;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.infra.security.AuthorizationService;
import com.felipe.projectmanagerapi.infra.security.UserPrincipal;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.repositories.WorkspaceRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.AccessDeniedException;
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

  public WorkspaceResponseDTO create(@Valid @NotNull WorkspaceCreateOrUpdateDTO workspaceDTO) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Workspace workspace = new Workspace();
    workspace.setName(workspaceDTO.name());
    workspace.setOwner(userPrincipal.getUser());

    Workspace createdWorkspace = this.workspaceRepository.save(workspace);
    return this.workspaceMapper.toDTO(createdWorkspace);
  }

  public WorkspaceResponseDTO update(@NotNull String workspaceId, @Valid @NotNull WorkspaceCreateOrUpdateDTO workspaceDTO) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Workspace updatedWorkspace = this.workspaceRepository.findById(workspaceId)
      .map(workspace -> {
        if(!userPrincipal.getUser().getId().equals(workspace.getOwner().getId())) {
          throw new AccessDeniedException("Acesso negado: Você não tem permissão para modificar este recurso");
        }
        workspace.setName(workspaceDTO.name());
        return this.workspaceRepository.save(workspace);
      })
      .orElseThrow(() -> new RecordNotFoundException("Workspace não encontrado"));
    return this.workspaceMapper.toDTO(updatedWorkspace);
  }

  public List<WorkspaceResponseDTO> getAllUserWorkspaces() {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    return this.workspaceRepository.findAllByOwnerId(userPrincipal.getUser().getId())
      .stream()
      .map(this.workspaceMapper::toDTO)
      .toList();
  }
}
