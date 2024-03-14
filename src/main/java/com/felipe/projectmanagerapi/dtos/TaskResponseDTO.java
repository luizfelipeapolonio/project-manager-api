package com.felipe.projectmanagerapi.dtos;

import java.time.LocalDateTime;

public record TaskResponseDTO(
  String id,
  String name,
  String description,
  String cost,
  LocalDateTime createdAt,
  LocalDateTime updatedAt,
  String projectId,
  String ownerId
) {}
