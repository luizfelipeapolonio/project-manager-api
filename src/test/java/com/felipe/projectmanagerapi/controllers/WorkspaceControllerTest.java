package com.felipe.projectmanagerapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipe.projectmanagerapi.dtos.*;
import com.felipe.projectmanagerapi.dtos.mappers.UserMapper;
import com.felipe.projectmanagerapi.dtos.mappers.WorkspaceMapper;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.exceptions.MemberAlreadyExistsException;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.exceptions.WorkspaceIsNotEmptyException;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.services.WorkspaceService;
import com.felipe.projectmanagerapi.utils.ConvertDateFormat;
import com.felipe.projectmanagerapi.utils.CustomResponseBody;
import com.felipe.projectmanagerapi.utils.GenerateMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "test")
public class WorkspaceControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  WorkspaceService workspaceService;

  @SpyBean
  WorkspaceMapper workspaceMapper;

  @SpyBean
  UserMapper userMapper;

  private AutoCloseable closeable;
  private String baseUrl;
  private GenerateMocks dataMock;

  @BeforeEach
  void setUp() {
    this.closeable = MockitoAnnotations.openMocks(this);
    this.baseUrl = "/api/workspaces";
    this.dataMock = new GenerateMocks();
  }

  @AfterEach
  void tearDown() throws Exception {
    this.closeable.close();
  }

  @Test
  @DisplayName("create - Should return a success response with created status code")
  void workspaceCreateSuccess() throws Exception {
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    WorkspaceResponseDTO createdWorkspace = new WorkspaceResponseDTO(
      workspace.getId(),
      workspace.getName(),
      workspace.getOwner().getId(),
      workspace.getCreatedAt(),
      workspace.getUpdatedAt()
    );
    WorkspaceCreateOrUpdateDTO workspaceDTO = new WorkspaceCreateOrUpdateDTO("Workspace 1");
    String jsonBody = this.objectMapper.writeValueAsString(workspaceDTO);

    when(this.workspaceService.create(workspaceDTO)).thenReturn(workspace);
    when(this.workspaceMapper.toWorkspaceResponseDTO(workspace)).thenReturn(createdWorkspace);

    this.mockMvc.perform(post(this.baseUrl)
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.CREATED.value()))
      .andExpect(jsonPath("$.message").value("Workspace criado com sucesso"))
      .andExpect(jsonPath("$.data.id").value(createdWorkspace.id()))
      .andExpect(jsonPath("$.data.name").value(createdWorkspace.name()))
      .andExpect(jsonPath("$.data.ownerId").value(createdWorkspace.ownerId()))
      .andExpect(jsonPath("$.data.createdAt").value(createdWorkspace.createdAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(createdWorkspace.updatedAt().toString()));

    verify(this.workspaceService, times(1)).create(workspaceDTO);
    verify(this.workspaceMapper, times(1)).toWorkspaceResponseDTO(workspace);
  }

  @Test
  @DisplayName("update - Should return a success response with OK status code and the updated workspace")
  void updateWorkspaceSuccess() throws Exception {
    WorkspaceCreateOrUpdateDTO workspaceDTO = new WorkspaceCreateOrUpdateDTO("Updated Name");
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    workspace.setName(workspaceDTO.name());

    WorkspaceResponseDTO updatedWorkspace = new WorkspaceResponseDTO(
      workspace.getId(),
      workspace.getName(),
      workspace.getOwner().getId(),
      workspace.getCreatedAt(),
      workspace.getUpdatedAt()
    );
    String jsonBody = this.objectMapper.writeValueAsString(workspaceDTO);

    when(this.workspaceService.update("01", workspaceDTO)).thenReturn(workspace);
    when(this.workspaceMapper.toWorkspaceResponseDTO(workspace)).thenReturn(updatedWorkspace);

    this.mockMvc.perform(patch(this.baseUrl + "/01")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Workspace atualizado com sucesso"))
      .andExpect(jsonPath("$.data.id").value(updatedWorkspace.id()))
      .andExpect(jsonPath("$.data.name").value(updatedWorkspace.name()))
      .andExpect(jsonPath("$.data.ownerId").value(updatedWorkspace.ownerId()))
      .andExpect(jsonPath("$.data.createdAt").value(updatedWorkspace.createdAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(updatedWorkspace.updatedAt().toString()));

    verify(this.workspaceService, times(1)).update("01", workspaceDTO);
    verify(this.workspaceMapper, times(1)).toWorkspaceResponseDTO(workspace);
  }

  @Test
  @DisplayName("update - Should return an error response with forbidden status code")
  void updateWorkspaceFailsByDifferentOwnerId() throws Exception {
    WorkspaceCreateOrUpdateDTO workspaceDTO = new WorkspaceCreateOrUpdateDTO("Updated Name");
    String jsonBody = this.objectMapper.writeValueAsString(workspaceDTO);

    when(this.workspaceService.update("01", workspaceDTO))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para modificar este recurso"));

    this.mockMvc.perform(patch(this.baseUrl + "/01")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para modificar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).update("01", workspaceDTO);
    verify(this.workspaceMapper, never()).toWorkspaceResponseDTO(any(Workspace.class));
  }

  @Test
  @DisplayName("update - Should return an error response with not found status code")
  void updateWorkspaceFailsByWorkspaceNotFound() throws Exception {
    WorkspaceCreateOrUpdateDTO workspaceDTO = new WorkspaceCreateOrUpdateDTO("Updated Name");
    String jsonBody = this.objectMapper.writeValueAsString(workspaceDTO);

    when(this.workspaceService.update("01", workspaceDTO))
      .thenThrow(new RecordNotFoundException("Workspace não encontrado"));

    this.mockMvc.perform(patch(this.baseUrl + "/01")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Workspace não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).update("01", workspaceDTO);
    verify(this.workspaceMapper, never()).toWorkspaceResponseDTO(any(Workspace.class));
  }

  @Test
  @DisplayName("getAllUserWorkspaces - Should return a success response with OK status code and a list of WorkspaceResponseDTO")
  void getAllUserWorkspacesSuccess() throws Exception {
    List<Workspace> workspaces = this.dataMock.getWorkspaces();
    List<WorkspaceResponseDTO> workspacesDTO = workspaces
      .stream()
      .map(workspace -> new WorkspaceResponseDTO(
        workspace.getId(),
        workspace.getName(),
        workspace.getOwner().getId(),
        workspace.getCreatedAt(),
        workspace.getUpdatedAt()))
      .toList();

    CustomResponseBody<List<WorkspaceResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os seus workspaces");
    response.setData(workspacesDTO);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.workspaceService.getAllUserWorkspaces()).thenReturn(workspaces);

    this.mockMvc.perform(get(this.baseUrl).accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.workspaceService, times(1)).getAllUserWorkspaces();
    verify(this.workspaceMapper, times(3)).toWorkspaceResponseDTO(any(Workspace.class));
  }

  @Test
  @DisplayName("getAllMembers - Should return a success response with OK status code and all members of the workspace")
  void getAllMembersSuccess() throws Exception {
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    WorkspaceResponseDTO workspaceDTO = new WorkspaceResponseDTO(
      workspace.getId(),
      workspace.getName(),
      workspace.getOwner().getId(),
      workspace.getCreatedAt(),
      workspace.getUpdatedAt()
    );
    List<UserResponseDTO> usersDTO = workspace.getMembers()
      .stream()
      .map(member -> new UserResponseDTO(
        member.getId(),
        member.getName(),
        member.getEmail(),
        member.getRole().getName(),
        member.getCreatedAt(),
        member.getUpdatedAt()
      ))
      .toList();
    WorkspaceMembersResponseDTO workspaceMembersDTO = new WorkspaceMembersResponseDTO(workspaceDTO, usersDTO);

    CustomResponseBody<WorkspaceMembersResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os membros do workspace");
    response.setData(workspaceMembersDTO);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.workspaceService.getById("01")).thenReturn(workspace);
    when(this.workspaceMapper.toWorkspaceResponseDTO(workspace)).thenReturn(workspaceDTO);

    this.mockMvc.perform(get(this.baseUrl + "/01/members")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.workspaceService, times(1)).getById("01");
    verify(this.workspaceMapper, times(1)).toWorkspaceResponseDTO(workspace);
    verify(this.userMapper, times(3)).toDTO(any(User.class));
  }

  @Test
  @DisplayName("getAllMembers - Should return an error response with not found status code")
  void getAllMembersFailsByWorkspaceNotFound() throws Exception {
    when(this.workspaceService.getById("01"))
      .thenThrow(new RecordNotFoundException("Workspace de ID: '01' não encontrado"));

    this.mockMvc.perform(get(this.baseUrl + "/01/members")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Workspace de ID: '01' não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).getById("01");
    verify(this.workspaceMapper, never()).toWorkspaceResponseDTO(any(Workspace.class));
    verify(this.userMapper, never()).toDTO(any(User.class));
  }

  @Test
  @DisplayName("getAllMembers - Should return an error response with forbidden status code")
  void getAllMembersFailsByAccessDenied() throws Exception {
    when(this.workspaceService.getById("01"))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para acessar este recurso"));

    this.mockMvc.perform(get(this.baseUrl + "/01/members")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para acessar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).getById("01");
    verify(this.workspaceMapper, never()).toWorkspaceResponseDTO(any(Workspace.class));
    verify(this.userMapper, never()).toDTO(any(User.class));
  }

  @Test
  @DisplayName("delete - Should return a success response with OK status code and the deleted workspace")
  void deleteWorkspaceSuccess() throws Exception {
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    WorkspaceResponseDTO deletedWorkspace = new WorkspaceResponseDTO(
      workspace.getId(),
      workspace.getName(),
      workspace.getOwner().getId(),
      workspace.getCreatedAt(),
      workspace.getUpdatedAt()
    );

    Map<String, WorkspaceResponseDTO> response = new HashMap<>();
    response.put("deletedWorkspace", deletedWorkspace);

    when(this.workspaceService.delete("01")).thenReturn(workspace);
    when(this.workspaceMapper.toWorkspaceResponseDTO(workspace)).thenReturn(deletedWorkspace);

    this.mockMvc.perform(delete(this.baseUrl + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Workspace deletado com sucesso"))
      .andExpect(jsonPath("$.data.deletedWorkspace.id").value(response.get("deletedWorkspace").id()))
      .andExpect(jsonPath("$.data.deletedWorkspace.name").value(response.get("deletedWorkspace").name()))
      .andExpect(jsonPath("$.data.deletedWorkspace.ownerId").value(response.get("deletedWorkspace").ownerId()))
      .andExpect(jsonPath("$.data.deletedWorkspace.createdAt").value(response.get("deletedWorkspace").createdAt().toString()))
      .andExpect(jsonPath("$.data.deletedWorkspace.updatedAt").value(response.get("deletedWorkspace").updatedAt().toString()));

    verify(this.workspaceService, times(1)).delete("01");
    verify(this.workspaceMapper, times(1)).toWorkspaceResponseDTO(workspace);
  }

  @Test
  @DisplayName("delete - Should return an error response with not found status code")
  void deleteWorkspaceFailsByWorkspaceNotFound() throws Exception {
    when(this.workspaceService.delete("01"))
      .thenThrow(new RecordNotFoundException("Workspace de ID: '01' não encontrado"));

    this.mockMvc.perform(delete(this.baseUrl + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Workspace de ID: '01' não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).delete("01");
    verify(this.workspaceMapper, never()).toWorkspaceResponseDTO(any(Workspace.class));
  }

  @Test
  @DisplayName("delete - Should return an error response with forbidden status code")
  void deleteWorkspaceFailsByDifferentWorkspaceOwnerId() throws Exception {
    when(this.workspaceService.delete("01"))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para manipular este recurso"));

    this.mockMvc.perform(delete(this.baseUrl + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para manipular este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).delete("01");
    verify(this.workspaceMapper, never()).toWorkspaceResponseDTO(any(Workspace.class));
  }

  @Test
  @DisplayName("delete - Should return an error response with bad request status code")
  void deleteWorkspaceFailsByWorkspaceIsNotEmpty() throws Exception {
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    workspace.setProjects(List.of(this.dataMock.getProjects().get(1)));

    when(this.workspaceService.delete("01"))
      .thenThrow(new WorkspaceIsNotEmptyException(workspace));

    this.mockMvc.perform(delete(this.baseUrl + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
      .andExpect(jsonPath("$.message").value(
        "Não foi possível excluir o workspace. O workspace de ID: '01' não está vazio. " +
          "Quantidade de projetos: 1. Exclua todos os projetos antes de excluir o workspace"
      ))
      .andExpect(jsonPath(".data").doesNotExist());

    verify(this.workspaceService, times(1)).delete("01");
    verify(this.workspaceMapper, never()).toWorkspaceResponseDTO(any(Workspace.class));
  }

  @Test
  @DisplayName("insertMember - Should return a success response with OK status code and the workspace with the inserted member")
  void insertMemberSuccess() throws Exception {
    Workspace workspace = this.dataMock.getWorkspaces().get(0);

    WorkspaceResponseDTO workspaceDTO = new WorkspaceResponseDTO(
      workspace.getId(),
      workspace.getName(),
      workspace.getOwner().getId(),
      workspace.getCreatedAt(),
      workspace.getUpdatedAt()
    );
    List<UserResponseDTO> usersDTO = workspace.getMembers()
      .stream()
      .map(member -> new UserResponseDTO(
        member.getId(),
        member.getName(),
        member.getEmail(),
        member.getRole().getName(),
        member.getCreatedAt(),
        member.getUpdatedAt()
      ))
      .toList();

    CustomResponseBody<WorkspaceMembersResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Membro inserido no workspace com sucesso");
    response.setData(new WorkspaceMembersResponseDTO(workspaceDTO, usersDTO));

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.workspaceService.insertMember("01", "02")).thenReturn(workspace);
    when(this.workspaceMapper.toWorkspaceResponseDTO(workspace)).thenReturn(workspaceDTO);

    this.mockMvc.perform(patch(this.baseUrl + "/01/members/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.workspaceService, times(1)).insertMember("01", "02");
    verify(this.workspaceMapper, times(1)).toWorkspaceResponseDTO(workspace);
    verify(this.userMapper, times(3)).toDTO(any(User.class));
  }

  @Test
  @DisplayName("insertMember - Should return an error response with forbidden status code")
  void insertMemberFailsByDifferentWorkspaceOwnerId() throws Exception{
    when(this.workspaceService.insertMember("01", "02"))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para alterar este recurso"));

    this.mockMvc.perform(patch(this.baseUrl + "/01/members/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para alterar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).insertMember("01", "02");
    verify(this.workspaceMapper, never()).toWorkspaceResponseDTO(any(Workspace.class));
    verify(this.userMapper, never()).toDTO(any(User.class));
  }

  @Test
  @DisplayName("insertMember - Should return an error response with not found status code")
  void insertMemberFailsByWorkspaceNotFound() throws Exception {
    when(this.workspaceService.insertMember("01", "02"))
      .thenThrow(new RecordNotFoundException("Workspace com ID: '01' não encontrado"));

    this.mockMvc.perform(patch(this.baseUrl + "/01/members/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Workspace com ID: '01' não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).insertMember("01", "02");
    verify(this.workspaceMapper, never()).toWorkspaceResponseDTO(any(Workspace.class));
    verify(this.userMapper, never()).toDTO(any(User.class));
  }

  @Test
  @DisplayName("insertMember - Should return an error response with conflict status code")
  void insertMemberFailsByMemberAlreadyExists() throws Exception {
    when(this.workspaceService.insertMember("01", "02"))
      .thenThrow(new MemberAlreadyExistsException("02", "01"));

    this.mockMvc.perform(patch(this.baseUrl + "/01/members/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
      .andExpect(jsonPath("$.message").value("O usuário de ID: '02' já é membro do workspace de ID: '01'."))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).insertMember("01", "02");
    verify(this.workspaceMapper, never()).toWorkspaceResponseDTO(any(Workspace.class));
    verify(this.userMapper, never()).toDTO(any(User.class));
  }

  @Test
  @DisplayName("removeMember - Should return a success response with OK status code, the workspace and its members")
  void removeMemberSuccess() throws Exception {
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    workspace.removeMember(this.dataMock.getUsers().get(1));

    WorkspaceResponseDTO workspaceDTO = new WorkspaceResponseDTO(
      workspace.getId(),
      workspace.getName(),
      workspace.getOwner().getId(),
      workspace.getCreatedAt(),
      workspace.getUpdatedAt()
    );
    List<UserResponseDTO> usersDTO = workspace.getMembers()
      .stream()
      .map(member -> new UserResponseDTO(
        member.getId(),
        member.getName(),
        member.getEmail(),
        member.getRole().getName(),
        member.getCreatedAt(),
        member.getUpdatedAt()
      ))
      .toList();

    CustomResponseBody<WorkspaceMembersResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Membro removido do workspace com sucesso");
    response.setData(new WorkspaceMembersResponseDTO(workspaceDTO, usersDTO));

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.workspaceService.removeMember("01", "02")).thenReturn(workspace);
    when(this.workspaceMapper.toWorkspaceResponseDTO(workspace)).thenReturn(workspaceDTO);

    this.mockMvc.perform(delete(this.baseUrl + "/01/members/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.workspaceService, times(1)).removeMember("01", "02");
    verify(this.workspaceMapper, times(1)).toWorkspaceResponseDTO(workspace);
    verify(this.userMapper, times(2)).toDTO(any(User.class));
  }

  @Test
  @DisplayName("removeMember - Should return an error response with a not found status code")
  void removeMemberFailsByWorkspaceNotFound() throws Exception {
    when(this.workspaceService.removeMember("01", "02"))
      .thenThrow(new RecordNotFoundException("Workspace de ID: '01' não encontrado"));

    this.mockMvc.perform(delete(this.baseUrl + "/01/members/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Workspace de ID: '01' não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).removeMember("01", "02");
    verify(this.workspaceMapper, never()).toWorkspaceResponseDTO(any(Workspace.class));
    verify(this.userMapper, never()).toDTO(any(User.class));
  }

  @Test
  @DisplayName("removeMember - Should return an error response with forbidden status code")
  void removeMemberFailsByDifferentWorkspaceOwnerId() throws Exception {
    when(this.workspaceService.removeMember("01", "02"))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para alterar este recurso"));

    this.mockMvc.perform(delete(this.baseUrl + "/01/members/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para alterar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).removeMember("01", "02");
    verify(this.workspaceMapper, never()).toWorkspaceResponseDTO(any(Workspace.class));
    verify(this.userMapper, never()).toDTO(any(User.class));
  }

  @Test
  @DisplayName("removeMember - Should return an error response with a not found status code")
  void removeMemberFailsByMemberNotFound() throws Exception {
    when(this.workspaceService.removeMember("01", "02"))
      .thenThrow(new RecordNotFoundException("Membro de ID: '02' não encontrado no workspace de ID: '01'"));

    this.mockMvc.perform(delete(this.baseUrl + "/01/members/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Membro de ID: '02' não encontrado no workspace de ID: '01'"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).removeMember("01", "02");
    verify(this.workspaceMapper, never()).toWorkspaceResponseDTO(any(Workspace.class));
    verify(this.userMapper, never()).toDTO(any(User.class));
  }

  @Test
  @DisplayName("getById - Should return a success response with OK status code and the workspace with a full workspace response DTO")
  void getByIdSuccess() throws Exception {
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    workspace.setProjects(List.of(this.dataMock.getProjects().get(1)));

    WorkspaceResponseDTO workspaceDTO = new WorkspaceResponseDTO(
      workspace.getId(),
      workspace.getName(),
      workspace.getOwner().getId(),
      workspace.getCreatedAt(),
      workspace.getUpdatedAt()
    );
    List<UserResponseDTO> members = workspace.getMembers()
      .stream()
      .map(member -> new UserResponseDTO(
        member.getId(),
        member.getName(),
        member.getEmail(),
        member.getRole().getName(),
        member.getCreatedAt(),
        member.getUpdatedAt()
      ))
      .toList();
    List<ProjectResponseDTO> projects = workspace.getProjects()
      .stream()
      .map(project -> new ProjectResponseDTO(
        project.getId(),
        project.getName(),
        project.getPriority().getValue(),
        project.getCategory(),
        project.getDescription(),
        project.getBudget().toString(),
        ConvertDateFormat.convertDateToFormattedString(project.getDeadline()),
        project.getCreatedAt(),
        project.getUpdatedAt(),
        project.getOwner().getId(),
        project.getWorkspace().getId()
      ))
      .toList();
    WorkspaceFullResponseDTO workspaceFullDTO = new WorkspaceFullResponseDTO(workspaceDTO, projects, members);

    CustomResponseBody<WorkspaceFullResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Workspace encontrado");
    response.setData(workspaceFullDTO);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.workspaceService.getById("01")).thenReturn(workspace);

    this.mockMvc.perform(get(this.baseUrl + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.workspaceService, times(1)).getById("01");
  }

  @Test
  @DisplayName("getById - Should return an error response with not found status code if the workspace is not found")
  void getByIdFailsByWorkspaceNotFound() throws Exception {
    when(this.workspaceService.getById("01"))
      .thenThrow(new RecordNotFoundException("Workspace de ID: '01' não encontrado"));

    this.mockMvc.perform(get(this.baseUrl + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Workspace de ID: '01' não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).getById("01");
    verify(this.workspaceMapper, never()).toWorkspaceResponseDTO(any(Workspace.class));
    verify(this.userMapper, never()).toDTO(any(User.class));
  }

  @Test
  @DisplayName("getById - Should return an error response with forbidden status code")
  void getByIdFailsByAccessDenied() throws Exception {
    when(this.workspaceService.getById("01"))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para acessar este recurso"));

    this.mockMvc.perform(get(this.baseUrl + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para acessar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).getById("01");
    verify(this.workspaceMapper, never()).toWorkspaceResponseDTO(any(Workspace.class));
    verify(this.userMapper, never()).toDTO(any(User.class));
  }
}
