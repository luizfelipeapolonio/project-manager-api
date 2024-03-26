package com.felipe.projectmanagerapi.repositories;

import com.felipe.projectmanagerapi.models.Project;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, String> {

  @Query("SELECT p FROM Project p WHERE p.workspace.id=:workspaceId AND p.owner.id=:ownerId")
  List<Project> findAllByWorkspaceIdAndOwnerId(@Param("workspaceId") String workspaceId, @Param("ownerId") String ownerId);

  @Query("SELECT p FROM Project p WHERE p.workspace.id=:workspaceId")
  List<Project> findAllByWorkspaceId(@Param("workspaceId") String workspaceId, Sort sort);

  @Query("SELECT p FROM Project p WHERE p.owner.id=:userId")
  List<Project> findAllByUserId(@Param("userId") String userId);
}
