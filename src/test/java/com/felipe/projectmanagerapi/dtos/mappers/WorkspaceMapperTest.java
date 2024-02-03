package com.felipe.projectmanagerapi.dtos.mappers;

import com.felipe.projectmanagerapi.dtos.WorkspaceResponseDTO;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.utils.GenerateMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDateTime;

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
}
