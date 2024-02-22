package com.felipe.projectmanagerapi.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectResponseDTO(
  String id,
  String name,
  String priority,
  String category,
  String description,
  String budget,
  String deadline,
  LocalDateTime createdAt,
  LocalDateTime updatedAt,
  String ownerId,
  String workspaceId
) {
}
