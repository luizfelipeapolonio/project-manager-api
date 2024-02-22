package com.felipe.projectmanagerapi.dtos.mappers;

import com.felipe.projectmanagerapi.dtos.ProjectResponseDTO;
import com.felipe.projectmanagerapi.enums.PriorityLevel;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.utils.ConvertDateFormat;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class ProjectMapper {
  public ProjectResponseDTO toDTO(Project project) {
    if(project == null) return null;
    return new ProjectResponseDTO(
      project.getId(),
      project.getName(),
      project.getPriority().getValue(),
      project.getCategory(),
      project.getDescription(),
      project.getBudget().toString(),
      ConvertDateFormat.convertDateFromDatabaseToRightFormat(project.getDeadline()),
      project.getCreatedAt(),
      project.getUpdatedAt(),
      project.getOwner().getId(),
      project.getWorkspace().getId()
    );
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
