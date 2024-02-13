package com.felipe.projectmanagerapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipe.projectmanagerapi.dtos.UserResponseDTO;
import com.felipe.projectmanagerapi.dtos.WorkspaceCreateOrUpdateDTO;
import com.felipe.projectmanagerapi.dtos.WorkspaceMembersResponseDTO;
import com.felipe.projectmanagerapi.dtos.WorkspaceResponseDTO;
import com.felipe.projectmanagerapi.dtos.mappers.UserMapper;
import com.felipe.projectmanagerapi.dtos.mappers.WorkspaceMapper;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.exceptions.MemberAlreadyExistsException;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.services.WorkspaceService;
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

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    when(this.workspaceMapper.toDTO(workspace)).thenReturn(createdWorkspace);

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
    verify(this.workspaceMapper, times(1)).toDTO(workspace);
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
    when(this.workspaceMapper.toDTO(workspace)).thenReturn(updatedWorkspace);

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
    verify(this.workspaceMapper, times(1)).toDTO(workspace);
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
    verify(this.workspaceMapper, never()).toDTO(any(Workspace.class));
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
    verify(this.workspaceMapper, never()).toDTO(any(Workspace.class));
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
    verify(this.workspaceMapper, times(3)).toDTO(any(Workspace.class));
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

    String jsonBody = this.objectMapper.writeValueAsString(response);

    when(this.workspaceService.insertMember("01", "02")).thenReturn(workspace);
    when(this.workspaceMapper.toDTO(workspace)).thenReturn(workspaceDTO);

    this.mockMvc.perform(patch(this.baseUrl + "/01/member/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(content().json(jsonBody));

    verify(this.workspaceService, times(1)).insertMember("01", "02");
    verify(this.workspaceMapper, times(1)).toDTO(workspace);
    verify(this.userMapper, times(3)).toDTO(any(User.class));
  }

  @Test
  @DisplayName("insertMember - Should return an error response with forbidden status code")
  void insertMemberFailsByDifferentWorkspaceOwnerId() throws Exception{
    when(this.workspaceService.insertMember("01", "02"))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para alterar este recurso"));

    this.mockMvc.perform(patch(this.baseUrl + "/01/member/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para alterar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).insertMember("01", "02");
    verify(this.workspaceMapper, never()).toDTO(any(Workspace.class));
    verify(this.userMapper, never()).toDTO(any(User.class));
  }

  @Test
  @DisplayName("insertMember - Should return an error response with not found status code")
  void insertMemberFailsByWorkspaceNotFound() throws Exception {
    when(this.workspaceService.insertMember("01", "02"))
      .thenThrow(new RecordNotFoundException("Workspace com ID: '01' não encontrado"));

    this.mockMvc.perform(patch(this.baseUrl + "/01/member/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Workspace com ID: '01' não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).insertMember("01", "02");
    verify(this.workspaceMapper, never()).toDTO(any(Workspace.class));
    verify(this.userMapper, never()).toDTO(any(User.class));
  }

  @Test
  @DisplayName("insertMember - Should return an error response with conflict status code")
  void insertMemberFailsByMemberAlreadyExists() throws Exception {
    when(this.workspaceService.insertMember("01", "02"))
      .thenThrow(new MemberAlreadyExistsException("02", "01"));

    this.mockMvc.perform(patch(this.baseUrl + "/01/member/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
      .andExpect(jsonPath("$.message").value("O usuário de ID: '02' já é membro do workspace de ID: '01'."))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.workspaceService, times(1)).insertMember("01", "02");
    verify(this.workspaceMapper, never()).toDTO(any(Workspace.class));
    verify(this.userMapper, never()).toDTO(any(User.class));
  }
}
