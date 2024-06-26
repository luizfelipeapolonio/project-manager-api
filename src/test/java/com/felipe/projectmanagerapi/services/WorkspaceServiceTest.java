package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.WorkspaceCreateOrUpdateDTO;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.exceptions.WorkspaceIsNotEmptyException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
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
    WorkspaceCreateOrUpdateDTO workspaceDTO = new WorkspaceCreateOrUpdateDTO("Workspace 1");

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

    Workspace createdWorkspace = this.workspaceService.create(workspaceDTO);

    assertThat(createdWorkspace.getId()).isEqualTo(workspace.getId());
    assertThat(createdWorkspace.getName()).isEqualTo(workspace.getName());
    assertThat(createdWorkspace.getOwner().getId()).isEqualTo(workspace.getOwner().getId());
    assertThat(createdWorkspace.getCreatedAt()).isEqualTo(workspace.getCreatedAt());
    assertThat(createdWorkspace.getUpdatedAt()).isEqualTo(workspace.getUpdatedAt());

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).save(any(Workspace.class));
  }

  @Test
  @DisplayName("update - Should successfully update the workspace name")
  void updateWorkspaceSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));

    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    WorkspaceCreateOrUpdateDTO workspaceDTO = new WorkspaceCreateOrUpdateDTO("Updated Name");
    Workspace updatedWorkspaceEntity = this.dataMock.getWorkspaces().get(0);
    updatedWorkspaceEntity.setName(workspaceDTO.name());

    when(this.workspaceRepository.findById("01")).thenReturn(Optional.of(workspace));
    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.save(any(Workspace.class))).thenReturn(updatedWorkspaceEntity);

    Workspace updatedWorkspace = this.workspaceService.update("01", workspaceDTO);

    assertThat(updatedWorkspace.getId()).isEqualTo(updatedWorkspaceEntity.getId());
    assertThat(updatedWorkspace.getName()).isEqualTo(updatedWorkspaceEntity.getName());
    assertThat(updatedWorkspace.getOwner().getId()).isEqualTo(updatedWorkspaceEntity.getOwner().getId());
    assertThat(updatedWorkspace.getCreatedAt()).isEqualTo(updatedWorkspaceEntity.getCreatedAt());
    assertThat(updatedWorkspace.getUpdatedAt()).isEqualTo(updatedWorkspaceEntity.getUpdatedAt());

    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).save(any(Workspace.class));
  }

  @Test
  @DisplayName("update - Should throw an AccessDeniedException if the workspace owner id is different from authenticated user id")
  void updateWorkspaceFailsByDifferentOwnerId() {
    WorkspaceCreateOrUpdateDTO workspaceDTO = new WorkspaceCreateOrUpdateDTO("Updated Name");
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    Workspace workspace = this.dataMock.getWorkspaces().get(0);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.of(workspace));

    Exception thrown = catchException(() -> this.workspaceService.update("01", workspaceDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para modificar este recurso");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.workspaceRepository, never()).save(any(Workspace.class));
  }

  @Test
  @DisplayName("update - Should throw a RecordNotFoundException if the workspace is not found")
  void updateWorkspaceFailsByWorkspaceNotFound() {
    WorkspaceCreateOrUpdateDTO workspaceDTO = new WorkspaceCreateOrUpdateDTO("Updated Name");
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.workspaceService.update("01", workspaceDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Workspace com ID: '01' não encontrado");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.workspaceRepository, never()).save(any(Workspace.class));
  }

  @Test
  @DisplayName("getAllUserWorkspaces - Should successfully return all user workspaces")
  void getAllUserWorkspacesSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));
    List<Workspace> workspaces = this.dataMock.getWorkspaces();

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.findAllByOwnerId(userPrincipal.getUser().getId())).thenReturn(workspaces);

    List<Workspace> foundWorkspaces = this.workspaceService.getAllUserWorkspaces();

    assertThat(foundWorkspaces)
      .allSatisfy(workspace -> assertThat(workspace.getOwner().getId()).isEqualTo(userPrincipal.getUser().getId()))
      .hasSize(3);

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).findAllByOwnerId(userPrincipal.getUser().getId());
  }

  @Test
  @DisplayName("getById - Should successfully get a workspace by Id")
  void getByIdSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));
    Workspace workspace = this.dataMock.getWorkspaces().get(0);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.of(workspace));

    Workspace foundWorkspace = this.workspaceService.getById("01");

    assertThat(foundWorkspace.getId()).isEqualTo(workspace.getId());
    assertThat(foundWorkspace.getName()).isEqualTo(workspace.getName());
    assertThat(foundWorkspace.getOwner().getId()).isEqualTo(workspace.getOwner().getId());
    assertThat(foundWorkspace.getMembers().size()).isEqualTo(workspace.getMembers().size());
    assertThat(foundWorkspace.getCreatedAt()).isEqualTo(workspace.getCreatedAt());
    assertThat(foundWorkspace.getUpdatedAt()).isEqualTo(workspace.getUpdatedAt());

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).findById("01");
  }

  @Test
  @DisplayName("getById - Should throw a RecordNotFoundException if the workspace is not found")
  void getByIdFailsByWorkspaceNotFound() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.workspaceService.getById("01"));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Workspace de ID: '01' não encontrado");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).findById("01");
  }

  @Test
  @DisplayName("getById - Should throw an AccessDeniedException if the authenticated user is not the owner or member of the workspace")
  void getByIdFailsByNotBeingOwnerOrMemberOfWorkspace() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    User randomUser = this.dataMock.getUsers().get(2);

    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    workspace.setMembers(List.of(randomUser));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.of(workspace));

    Exception thrown = catchException(() -> this.workspaceService.getById("01"));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para acessar este recurso");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).findById("01");
  }

  @Test
  @DisplayName("delete - Should successfully delete a workspace and return it")
  void deleteWorkspaceSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));
    Workspace workspace = this.dataMock.getWorkspaces().get(0);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.of(workspace));
    doNothing().when(this.workspaceRepository).deleteById(workspace.getId());

    Workspace deletedWorkspace = this.workspaceService.delete("01");

    assertThat(deletedWorkspace.getId()).isEqualTo(workspace.getId());
    assertThat(deletedWorkspace.getName()).isEqualTo(workspace.getName());
    assertThat(deletedWorkspace.getOwner().getId()).isEqualTo(workspace.getOwner().getId());
    assertThat(deletedWorkspace.getMembers().size()).isEqualTo(workspace.getMembers().size());
    assertThat(deletedWorkspace.getCreatedAt()).isEqualTo(workspace.getCreatedAt());
    assertThat(deletedWorkspace.getUpdatedAt()).isEqualTo(workspace.getUpdatedAt());

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.workspaceRepository, times(1)).deleteById(workspace.getId());
  }

  @Test
  @DisplayName("delete - Should throw a RecordNotFoundException if the workspace is not found")
  void deleteWorkspaceFailsByWorkspaceNotFound() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.workspaceService.delete("01"));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Workspace de ID: '01' não encontrado");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.workspaceRepository, never()).deleteById(anyString());
  }

  @Test
  @DisplayName("delete - Should throw an AccessDeniedException if the workspace owner id is different from authenticated user id")
  void deleteWorkspaceFailsByDifferentWorkspaceOwnerId() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    Workspace workspace = this.dataMock.getWorkspaces().get(0);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.of(workspace));

    Exception thrown = catchException(() -> this.workspaceService.delete("01"));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para manipular este recurso");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.workspaceRepository, never()).deleteById(workspace.getId());
  }

  @Test
  @DisplayName("delete - Should throw a WorkspaceIsNotEmptyException if there are projects in the workspace")
  void deleteWorkspaceFailsByWorkspaceIsNotEmpty() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    workspace.setProjects(List.of(this.dataMock.getProjects().get(1)));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.of(workspace));

    Exception thrown = catchException(() -> this.workspaceService.delete("01"));

    assertThat(thrown)
      .isExactlyInstanceOf(WorkspaceIsNotEmptyException.class)
      .hasMessage(
        "Não foi possível excluir o workspace. O workspace de ID: '01' não está vazio. " +
        "Quantidade de projetos: 1. Exclua todos os projetos antes de excluir o workspace"
      );

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.workspaceRepository, never()).deleteById("01");
  }
}
