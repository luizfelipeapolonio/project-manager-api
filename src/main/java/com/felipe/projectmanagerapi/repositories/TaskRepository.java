package com.felipe.projectmanagerapi.repositories;

import com.felipe.projectmanagerapi.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, String> {

  @Query("SELECT t FROM Task t WHERE t.project.id=:projectId")
  List<Task> findAllByProjectId(@Param("projectId") String projectId);

  @Query("SELECT t FROM Task t WHERE t.owner.id=:ownerId")
  List<Task> findAllByOwnerId(@Param("ownerId") String ownerId);
}
