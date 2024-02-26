package com.felipe.projectmanagerapi.dtos.mappers;

import com.felipe.projectmanagerapi.dtos.ProjectResponseDTO;
import com.felipe.projectmanagerapi.dtos.UserResponseDTO;
import com.felipe.projectmanagerapi.dtos.WorkspaceFullResponseDTO;
import com.felipe.projectmanagerapi.dtos.WorkspaceResponseDTO;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.utils.GenerateMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkspaceMapperTest {

  @Spy
  WorkspaceMapper workspaceMapper;
  private GenerateMocks dataMock;
  private AutoCloseable closeable;

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
  @DisplayName("toDTO - Should successfully convert a workspace entity to a WorkspaceResponseDTO object")
  void convertWorkspaceEntityToDTOSuccess() {
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");
    WorkspaceResponseDTO workspaceResponseDTO = new WorkspaceResponseDTO(
      "01",
      "Workspace 1",
      "01",
      mockDateTime,
      mockDateTime
    );

    WorkspaceResponseDTO convertedWorkspaceDTO = this.workspaceMapper.toDTO(workspace);

    assertThat(convertedWorkspaceDTO.id()).isEqualTo(workspaceResponseDTO.id());
    assertThat(convertedWorkspaceDTO.name()).isEqualTo(workspaceResponseDTO.name());
    assertThat(convertedWorkspaceDTO.ownerId()).isEqualTo(workspaceResponseDTO.ownerId());
    assertThat(convertedWorkspaceDTO.createdAt()).isEqualTo(workspaceResponseDTO.createdAt());
    assertThat(convertedWorkspaceDTO.updatedAt()).isEqualTo(workspaceResponseDTO.updatedAt());
  }

  @Test
  @DisplayName("toWorkspaceFullResponseDTO - Should successfully convert a workspace entity into a WorkspaceFullResponseDTO")
  void convertEntityToWorkspaceFullResponseDTOSuccess() {
    Project projectMock = this.dataMock.getProjects().get(1);
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    workspace.setProjects(List.of(projectMock));

    WorkspaceFullResponseDTO convertedWorkspace = this.workspaceMapper.toWorkspaceFullResponseDTO(workspace);

    assertThat(convertedWorkspace.workspace().id()).isEqualTo(workspace.getId());
    assertThat(convertedWorkspace.workspace().name()).isEqualTo(workspace.getName());
    assertThat(convertedWorkspace.workspace().ownerId()).isEqualTo(workspace.getOwner().getId());
    assertThat(convertedWorkspace.workspace().createdAt()).isEqualTo(workspace.getCreatedAt());
    assertThat(convertedWorkspace.workspace().updatedAt()).isEqualTo(workspace.getUpdatedAt());
    assertThat(convertedWorkspace.members().stream().map(UserResponseDTO::id).toList())
      .containsExactlyInAnyOrderElementsOf(workspace.getMembers().stream().map(User::getId).toList());
    assertThat(convertedWorkspace.members().size()).isEqualTo(workspace.getMembers().size());
    assertThat(convertedWorkspace.projects().stream().map(ProjectResponseDTO::id).toList())
      .containsExactlyInAnyOrderElementsOf(workspace.getProjects().stream().map(Project::getId).toList());
    assertThat(convertedWorkspace.projects().size()).isEqualTo(workspace.getProjects().size());
  }
}
