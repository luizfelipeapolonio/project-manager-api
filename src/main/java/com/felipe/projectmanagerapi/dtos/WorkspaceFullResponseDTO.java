package com.felipe.projectmanagerapi.dtos;

import java.util.List;

public record WorkspaceFullResponseDTO(
  WorkspaceResponseDTO workspace,
  List<ProjectResponseDTO> projects,
  List<UserResponseDTO> members
) {
}
