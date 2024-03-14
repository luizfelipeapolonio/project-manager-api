package com.felipe.projectmanagerapi.dtos.mappers;

import com.felipe.projectmanagerapi.dtos.ProjectResponseDTO;
import com.felipe.projectmanagerapi.dtos.UserResponseDTO;
import com.felipe.projectmanagerapi.dtos.WorkspaceFullResponseDTO;
import com.felipe.projectmanagerapi.dtos.WorkspaceResponseDTO;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.utils.ConvertDateFormat;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkspaceMapper {
  public WorkspaceResponseDTO toWorkspaceResponseDTO(Workspace workspace) {
    if(workspace == null) return null;
    return new WorkspaceResponseDTO(
      workspace.getId(),
      workspace.getName(),
      workspace.getOwner().getId(),
      workspace.getCreatedAt(),
      workspace.getUpdatedAt()
    );
  }

  public WorkspaceFullResponseDTO toWorkspaceFullResponseDTO(Workspace workspace) {
    if(workspace == null) return null;
    WorkspaceResponseDTO workspaceDTO = this.toWorkspaceResponseDTO(workspace);
    List<ProjectResponseDTO> projects = workspace.getProjects()
      .stream()
      .map(project -> new ProjectResponseDTO(
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
      ))
      .toList();
    List<UserResponseDTO> members = workspace.getMembers()
      .stream()
      .map(member -> new UserResponseDTO(
        member.getId(),
        member.getName(),
        member.getEmail(),
        member.getRole().getName(),
        member.getCreatedAt(),
        member.getUpdatedAt()
      ))
      .toList();
    return new WorkspaceFullResponseDTO(workspaceDTO, projects, members);
  }
}
