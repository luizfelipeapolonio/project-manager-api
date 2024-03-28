package com.felipe.projectmanagerapi.exceptions;

public class ExistingResourcesException extends RuntimeException {
  public ExistingResourcesException(int workspaceCount, int projectCount, int taskCount) {
    super(
      "Não foi possível excluir! O usuário ainda possui: " + workspaceCount + " workspace(s), " +
      projectCount + " projeto(s), " + taskCount + " task(s)"
    );
  }
}
