package com.felipe.projectmanagerapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipe.projectmanagerapi.dtos.WorkspaceCreateDTO;
import com.felipe.projectmanagerapi.dtos.WorkspaceResponseDTO;
import com.felipe.projectmanagerapi.dtos.mappers.WorkspaceMapper;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.services.WorkspaceService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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

  @Autowired
  WorkspaceMapper workspaceMapper;

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
    WorkspaceResponseDTO createdWorkspace = this.workspaceMapper.toDTO(workspace);
    WorkspaceCreateDTO workspaceDTO = new WorkspaceCreateDTO("Workspace 1");
    String jsonBody = this.objectMapper.writeValueAsString(workspaceDTO);

    when(this.workspaceService.create(workspaceDTO)).thenReturn(createdWorkspace);

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
  }
}
