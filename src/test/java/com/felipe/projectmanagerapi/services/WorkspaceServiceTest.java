package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.WorkspaceCreateDTO;
import com.felipe.projectmanagerapi.dtos.WorkspaceResponseDTO;
import com.felipe.projectmanagerapi.dtos.mappers.WorkspaceMapper;
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
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.assertj.core.api.Assertions.assertThat;

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
    //assertThat(createdWorkspace.getMembers().get(0).getId()).isEqualTo(workspace.getOwner().getId());
  }
}
