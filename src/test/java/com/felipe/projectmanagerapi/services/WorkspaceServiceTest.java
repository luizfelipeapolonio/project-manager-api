package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.WorkspaceCreateDTO;
import com.felipe.projectmanagerapi.dtos.WorkspaceResponseDTO;
import com.felipe.projectmanagerapi.dtos.mappers.WorkspaceMapper;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.infra.security.AuthorizationService;
import com.felipe.projectmanagerapi.infra.security.UserPrincipal;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.repositories.WorkspaceRepository;
import com.felipe.projectmanagerapi.utils.GenerateMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

public class WorkspaceServiceTest {

  @Autowired
  @InjectMocks
  WorkspaceService workspaceService;

  @Mock
  WorkspaceRepository workspaceRepository;

  @Mock
  AuthorizationService authorizationService;

  @Spy
  WorkspaceMapper workspaceMapper;

  @Mock
  Authentication authentication;

  private AutoCloseable closeable;
  private GenerateMocks dataMock;

  @BeforeEach
  void setUp() {
    this.closeable = MockitoAnnotations.openMocks(this);
    this.dataMock = new GenerateMocks();
  }

  @AfterEach
  void tearDown() throws Exception {
    this.closeable.close();
  }

  @Test
  @DisplayName("create - Should successfully create a workspace and return it")
  void workspaceCreateSuccess() {
    User user = this.dataMock.getUsers().get(0);
    UserPrincipal userPrincipal = new UserPrincipal(user);
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    WorkspaceCreateDTO workspaceDTO = new WorkspaceCreateDTO("Workspace 1");

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

    WorkspaceResponseDTO createdWorkspace = this.workspaceService.create(workspaceDTO);

    assertThat(createdWorkspace.id()).isEqualTo(workspace.getId());
    assertThat(createdWorkspace.name()).isEqualTo(workspace.getName());
    assertThat(createdWorkspace.ownerId()).isEqualTo(workspace.getOwner().getId());
    assertThat(createdWorkspace.createdAt()).isEqualTo(workspace.getCreatedAt());
    assertThat(createdWorkspace.updatedAt()).isEqualTo(workspace.getUpdatedAt());

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).save(any(Workspace.class));
    verify(this.workspaceMapper, times(1)).toDTO(workspace);
  }

  @Test
  @DisplayName("update - Should successfully update the workspace name")
  void updateWorkspaceSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));

    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    WorkspaceCreateDTO workspaceDTO = new WorkspaceCreateDTO("Updated Name");
    Workspace updatedWorkspaceEntity = this.dataMock.getWorkspaces().get(0);
    updatedWorkspaceEntity.setName(workspaceDTO.name());

    when(this.workspaceRepository.findById("01")).thenReturn(Optional.of(workspace));
    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.save(any(Workspace.class))).thenReturn(updatedWorkspaceEntity);

    WorkspaceResponseDTO updatedWorkspace = this.workspaceService.update("01", workspaceDTO);

    assertThat(updatedWorkspace.id()).isEqualTo(updatedWorkspaceEntity.getId());
    assertThat(updatedWorkspace.name()).isEqualTo(updatedWorkspaceEntity.getName());
    assertThat(updatedWorkspace.ownerId()).isEqualTo(updatedWorkspaceEntity.getOwner().getId());
    assertThat(updatedWorkspace.createdAt()).isEqualTo(updatedWorkspaceEntity.getCreatedAt());
    assertThat(updatedWorkspace.updatedAt()).isEqualTo(updatedWorkspaceEntity.getUpdatedAt());

    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).save(any(Workspace.class));
    verify(this.workspaceMapper, times(1)).toDTO(updatedWorkspaceEntity);
  }

  @Test
  @DisplayName("update - Should throw an AccessDeniedException if the workspace owner id is different from authenticated user id")
  void updateWorkspaceFailsByDifferentOwnerId() {
    WorkspaceCreateDTO workspaceDTO = new WorkspaceCreateDTO("Updated Name");
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    Workspace workspace = this.dataMock.getWorkspaces().get(0);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.of(workspace));

    Exception thrown = catchException(() -> this.workspaceService.update("01", workspaceDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.workspaceRepository, never()).save(any(Workspace.class));
    verify(this.workspaceMapper, never()).toDTO(any(Workspace.class));
  }

  @Test
  @DisplayName("update - Should throw a RecordNotFoundException if the workspace is not found")
  void updateWorkspaceFailsByWorkspaceNotFound() {
    WorkspaceCreateDTO workspaceDTO = new WorkspaceCreateDTO("Updated Name");
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.workspaceService.update("01", workspaceDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Workspace não encontrado");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.workspaceRepository, never()).save(any(Workspace.class));
    verify(this.workspaceMapper, never()).toDTO(any(Workspace.class));
  }
}
