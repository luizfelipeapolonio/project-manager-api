package com.felipe.projectmanagerapi.repositories;

import com.felipe.projectmanagerapi.models.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, String> {

  @Query("SELECT w FROM Workspace w WHERE w.owner.id = :id")
  List<Workspace> findAllByOwnerId(@Param("id") String ownerId);
}
