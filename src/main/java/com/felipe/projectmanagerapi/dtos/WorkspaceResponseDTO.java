package com.felipe.projectmanagerapi.dtos;

import java.time.LocalDateTime;

public record WorkspaceResponseDTO(
  String id,
  String name,
  String ownerId,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {}
