package com.felipe.projectmanagerapi.repositories;

import com.felipe.projectmanagerapi.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, String> {
}
