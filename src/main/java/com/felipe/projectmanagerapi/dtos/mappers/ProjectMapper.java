package com.felipe.projectmanagerapi.dtos.mappers;

import com.felipe.projectmanagerapi.dtos.ProjectFullResponseDTO;
import com.felipe.projectmanagerapi.dtos.ProjectResponseDTO;
import com.felipe.projectmanagerapi.dtos.TaskResponseDTO;
import com.felipe.projectmanagerapi.enums.PriorityLevel;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.utils.ConvertDateFormat;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectMapper {
  public ProjectResponseDTO toProjectResponseDTO(Project project) {
    if(project == null) return null;
    return new ProjectResponseDTO(
      project.getId(),
      project.getName(),
      project.getPriority().getValue(),
      project.getCategory(),
      project.getDescription(),
      project.getBudget().toString(),
      project.getCost().toString(),
      ConvertDateFormat.convertDateToFormattedString(project.getDeadline()),
      project.getCreatedAt(),
      project.getUpdatedAt(),
      project.getOwner().getId(),
      project.getWorkspace().getId()
    );
  }

  public ProjectFullResponseDTO toProjectFullResponseDTO(Project project) {
    if(project == null) return null;
    ProjectResponseDTO projectResponseDTO = this.toProjectResponseDTO(project);
    List<TaskResponseDTO> taskResponseDTOs = project.getTasks().stream()
      .map(task -> new TaskResponseDTO(
        task.getId(),
        task.getName(),
        task.getDescription(),
        task.getCost().toString(),
        task.getCreatedAt(),
        task.getUpdatedAt(),
        task.getProject().getId(),
        task.getOwner().getId()
      ))
      .toList();
    return new ProjectFullResponseDTO(projectResponseDTO, taskResponseDTOs);
  }

  public PriorityLevel convertValueToPriorityLevel(@NotNull String value) {
    return switch(value) {
      case "alta" -> PriorityLevel.HIGH;
      case "media" -> PriorityLevel.MEDIUM;
      case "baixa" -> PriorityLevel.LOW;
      default -> throw new IllegalArgumentException(
        "Não foi possível converter o valor: '" + value + "' para PriorityLevel"
      );
    };
  }
}
