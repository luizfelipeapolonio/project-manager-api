package com.felipe.projectmanagerapi.dtos;

import java.util.List;

public record WorkspaceMembersResponseDTO(
  WorkspaceResponseDTO workspace,
  List<UserResponseDTO> members
) {}
