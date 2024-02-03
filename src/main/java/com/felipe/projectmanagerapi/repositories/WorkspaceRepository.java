package com.felipe.projectmanagerapi.repositories;

import com.felipe.projectmanagerapi.models.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, String> {}
