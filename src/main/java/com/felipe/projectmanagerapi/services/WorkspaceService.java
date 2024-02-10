package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.WorkspaceCreateOrUpdateDTO;
import com.felipe.projectmanagerapi.dtos.WorkspaceMemberResponseDTO;
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
  //private final UserService userService;

  public WorkspaceService(
    WorkspaceRepository workspaceRepository,
    AuthorizationService authorizationService
    //UserService userService
  ) {
    this.workspaceRepository = workspaceRepository;
    this.authorizationService = authorizationService;
    //this.userService = userService;
  }

  public Workspace create(@Valid @NotNull WorkspaceCreateOrUpdateDTO workspaceDTO) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Workspace workspace = new Workspace();
    workspace.setName(workspaceDTO.name());
    workspace.setOwner(userPrincipal.getUser());

    return this.workspaceRepository.save(workspace);
  }

  public Workspace update(@NotNull String workspaceId, @Valid @NotNull WorkspaceCreateOrUpdateDTO workspaceDTO) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    return this.workspaceRepository.findById(workspaceId)
      .map(workspace -> {
        if(!userPrincipal.getUser().getId().equals(workspace.getOwner().getId())) {
          throw new AccessDeniedException("Acesso negado: Você não tem permissão para modificar este recurso");
        }
        workspace.setName(workspaceDTO.name());
        return this.workspaceRepository.save(workspace);
      })
      .orElseThrow(() -> new RecordNotFoundException("Workspace não encontrado"));
  }

  public List<Workspace> getAllUserWorkspaces() {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    return this.workspaceRepository.findAllByOwnerId(userPrincipal.getUser().getId());
  }

  public WorkspaceMemberResponseDTO insertMember(String workspaceId, String userId) {
    Authentication authentication = this.authorizationService.getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    return null;
  }
}
