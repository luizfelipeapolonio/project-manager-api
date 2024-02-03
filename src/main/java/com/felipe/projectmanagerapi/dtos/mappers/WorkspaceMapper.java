package com.felipe.projectmanagerapi.dtos.mappers;

import com.felipe.projectmanagerapi.dtos.WorkspaceResponseDTO;
import com.felipe.projectmanagerapi.models.Workspace;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceMapper {
  public WorkspaceResponseDTO toDTO(Workspace workspace) {
    if(workspace == null) return null;
    return new WorkspaceResponseDTO(
      workspace.getId(),
      workspace.getName(),
      workspace.getOwner().getId(),
      workspace.getCreatedAt(),
      workspace.getUpdatedAt()
    );
  }
}
