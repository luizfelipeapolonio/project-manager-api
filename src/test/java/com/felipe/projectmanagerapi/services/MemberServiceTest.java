package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.exceptions.MemberAlreadyExistsException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doNothing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

public class MemberServiceTest {

  @Autowired
  @InjectMocks
  MemberService memberService;

  @Mock
  WorkspaceRepository workspaceRepository;

  @Mock
  UserService userService;

  @Mock
  AuthorizationService authorizationService;

  @Mock
  ProjectService projectService;

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
  @DisplayName("insertMember - Should successfully insert a user as a member of the workspace and return it")
  void insertWorkspaceMemberSuccess() {
    User workspaceOwner = this.dataMock.getUsers().get(0);
    User workspaceMember = this.dataMock.getUsers().get(1);
    Workspace mockWorkspace = this.dataMock.getWorkspaces().get(0);
    UserPrincipal userPrincipal = new UserPrincipal(workspaceOwner);

    Workspace workspace = new Workspace();
    workspace.setId(mockWorkspace.getId());
    workspace.setName(mockWorkspace.getName());
    workspace.setOwner(workspaceOwner);
    workspace.setCreatedAt(mockWorkspace.getCreatedAt());
    workspace.setUpdatedAt(mockWorkspace.getUpdatedAt());

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.userService.getProfile(workspaceMember.getId())).thenReturn(workspaceMember);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.of(workspace));
    when(this.workspaceRepository.save(workspace)).thenReturn(workspace);

    Workspace insertedMember = this.memberService.insertMember("01", "02");

    assertThat(insertedMember.getId()).isEqualTo(workspace.getId());
    assertThat(insertedMember.getName()).isEqualTo(workspace.getName());
    assertThat(insertedMember.getOwner().getId()).isEqualTo(workspace.getOwner().getId());
    assertThat(insertedMember.getMembers()).contains(workspaceMember).hasSize(1);

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.workspaceRepository, times(1)).save(workspace);
    verify(this.userService, times(1)).getProfile(workspaceMember.getId());
  }

  @Test
  @DisplayName("insertMember - Should throw a RecordNotFoundException if workspace is not found")
  void insertWorkspaceMemberFailsByWorkspaceNotFound() {
    User user = this.dataMock.getUsers().get(0);
    UserPrincipal userPrincipal = new UserPrincipal(user);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.userService.getProfile(anyString())).thenReturn(any(User.class));
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.memberService.insertMember("01", "02"));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Workspace com ID: '01' não encontrado");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.userService, times(1)).getProfile(anyString());
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.workspaceRepository, never()).save(any(Workspace.class));
  }

  @Test
  @DisplayName("insertMember - Should throw an AccessDeniedException if the workspace owner id is different from authenticated user id")
  void insertMemberFailsByDifferentWorkspaceOwnerId() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    User workspaceMember = this.dataMock.getUsers().get(2);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.userService.getProfile(anyString())).thenReturn(workspaceMember);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.of(workspace));

    Exception thrown = catchException(() -> this.memberService.insertMember("01", "02"));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para alterar este recurso");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.userService, times(1)).getProfile(anyString());
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.workspaceRepository, never()).save(workspace);
  }

  @Test
  @DisplayName("insertMember - Should throw a MemberAlreadyExistsException if the given user is already a member of the workspace")
  void insertMemberFailsByMemberAlreadyExists() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));
    User workspaceMember = this.dataMock.getUsers().get(1);
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    workspace.setMembers(List.of(workspaceMember));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.userService.getProfile("02")).thenReturn(workspaceMember);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.of(workspace));

    Exception thrown = catchException(() -> this.memberService.insertMember("01", "02"));

    assertThat(thrown)
      .isExactlyInstanceOf(MemberAlreadyExistsException.class)
      .hasMessage("O usuário de ID: '02' já é membro do workspace de ID: '01'.");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.userService, times(1)).getProfile("02");
    verify(this.workspaceRepository, never()).save(workspace);
  }

  @Test
  @DisplayName("removeMember - Should successfully remove a member of the workspace")
  void removeMemberSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));
    User workspaceMember = this.dataMock.getUsers().get(1);
    Workspace workspace = this.dataMock.getWorkspaces().get(0);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.userService.getProfile("02")).thenReturn(workspaceMember);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.of(workspace));
    when(this.projectService.deleteAllFromOwnerAndWorkspace("01", "02")).thenReturn(any());
    when(this.workspaceRepository.save(workspace)).thenReturn(workspace);

    Workspace updatedWorkspace = this.memberService.removeMember("01", "02");

    assertThat(updatedWorkspace.getMembers()).doesNotContain(workspaceMember).hasSize(2);
    assertThat(updatedWorkspace.getId()).isEqualTo(workspace.getId());
    assertThat(updatedWorkspace.getName()).isEqualTo(workspace.getName());
    assertThat(updatedWorkspace.getOwner().getId()).isEqualTo(workspace.getOwner().getId());
    assertThat(updatedWorkspace.getCreatedAt()).isEqualTo(workspace.getCreatedAt());
    assertThat(updatedWorkspace.getUpdatedAt()).isEqualTo(workspace.getUpdatedAt());

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.userService, times(1)).getProfile("02");
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.workspaceRepository, times(1)).save(workspace);
  }

  @Test
  @DisplayName("removeMember - Should throw a RecordNotFoundException if the workspace is not found")
  void removeMemberFailsByWorkspaceNotFound() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));
    User workspaceMember = this.dataMock.getUsers().get(1);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.userService.getProfile("02")).thenReturn(workspaceMember);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.memberService.removeMember("01", "02"));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Workspace de ID: '01' não encontrado");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.userService, times(1)).getProfile("02");
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.workspaceRepository, never()).save(any(Workspace.class));
  }

  @Test
  @DisplayName("removeMember - Should throw an AccessDeniedException if the workspace owner id is different from authenticated user id")
  void removeMemberFailsByDifferentWorkspaceOwnerId() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    User workspaceMember = this.dataMock.getUsers().get(2);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.userService.getProfile("02")).thenReturn(workspaceMember);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.of(workspace));

    Exception thrown = catchException(() -> this.memberService.removeMember("01", "02"));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para alterar este recurso");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.userService, times(1)).getProfile("02");
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.workspaceRepository, never()).save(any(Workspace.class));
  }

  @Test
  @DisplayName("removeMember - Should throw a RecordNotFoundException if the member does not exist in the workspace")
  void removeMemberFailsByMemberNotFound() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));
    User workspaceMember = this.dataMock.getUsers().get(1);
    User randomUser = this.dataMock.getUsers().get(2);

    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    workspace.setMembers(List.of(randomUser));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.userService.getProfile("02")).thenReturn(workspaceMember);
    when(this.workspaceRepository.findById("01")).thenReturn(Optional.of(workspace));

    Exception thrown = catchException(() -> this.memberService.removeMember("01", "02"));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Membro de ID: '02' não encontrado no workspace de ID: '01'");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.userService, times(1)).getProfile("02");
    verify(this.workspaceRepository, times(1)).findById("01");
    verify(this.workspaceRepository, never()).save(any(Workspace.class));
  }
}
