package com.felipe.projectmanagerapi.dtos.mappers;

import com.felipe.projectmanagerapi.dtos.TaskResponseDTO;
import com.felipe.projectmanagerapi.models.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

  public TaskResponseDTO toDTO(Task task) {
    if(task == null) return null;
    return new TaskResponseDTO(
      task.getId(),
      task.getName(),
      task.getDescription(),
      task.getCost().toString(),
      task.getCreatedAt(),
      task.getUpdatedAt(),
      task.getProject().getId(),
      task.getOwner().getId());
  }
}
