package com.felipe.projectmanagerapi.dtos;

import java.util.List;

public record ProjectFullResponseDTO(
  ProjectResponseDTO project,
  List<TaskResponseDTO> tasks
) {}
