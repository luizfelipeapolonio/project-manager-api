package com.felipe.projectmanagerapi.repositories;

import com.felipe.projectmanagerapi.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, String> {
}
