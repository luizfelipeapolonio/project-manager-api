package com.felipe.projectmanagerapi.exceptions;

import com.felipe.projectmanagerapi.models.Workspace;

public class WorkspaceIsNotEmptyException extends RuntimeException {
  public WorkspaceIsNotEmptyException(Workspace workspace) {
    super("Não foi possível excluir o workspace. " +
      "O workspace de ID: '" + workspace.getId() + "' não está vazio. " +
      "Quantidade de projetos: " + workspace.getProjects().size() + ". " +
      "Exclua todos os projetos antes de excluir o workspace"
    );
  }
}
