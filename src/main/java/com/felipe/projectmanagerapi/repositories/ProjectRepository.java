package com.felipe.projectmanagerapi.repositories;

import com.felipe.projectmanagerapi.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, String> {

  @Query("SELECT p FROM Project p WHERE p.workspace.id=:workspaceId AND p.owner.id=:ownerId")
  List<Project> findAllByWorkspaceIdAndOwnerId(@Param("workspaceId") String workspaceId, @Param("ownerId") String ownerId);
}
