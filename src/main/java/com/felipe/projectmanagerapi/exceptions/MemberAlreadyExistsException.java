package com.felipe.projectmanagerapi.exceptions;

public class MemberAlreadyExistsException extends RuntimeException {
  public MemberAlreadyExistsException(String userId, String workspaceId) {
    super("O usuário de ID: '" + userId + "' já é membro do workspace de ID: '" + workspaceId + "'." );
  }
}
