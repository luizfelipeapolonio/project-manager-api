package com.felipe.projectmanagerapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipe.projectmanagerapi.dtos.ProjectCreateDTO;
import com.felipe.projectmanagerapi.dtos.ProjectResponseDTO;
import com.felipe.projectmanagerapi.dtos.mappers.ProjectMapper;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.exceptions.InvalidDateException;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.services.ProjectService;
import com.felipe.projectmanagerapi.utils.ConvertDateFormat;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "test")
public class ProjectControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  ProjectService projectService;

  @SpyBean
  ProjectMapper projectMapper;

  private AutoCloseable closeable;
  private GenerateMocks dataMock;
  private String baseUrl;

  @BeforeEach
  void setUp() {
    this.closeable = MockitoAnnotations.openMocks(this);
    this.dataMock = new GenerateMocks();
    this.baseUrl = "/api/projects";
  }

  @AfterEach
  void tearDown() throws Exception {
    this.closeable.close();
  }

  @Test
  @DisplayName("create - Should return a success response with created status code and the created project")
  void createProjectSuccess() throws Exception {
    Project project = this.dataMock.getProjects().get(1);
    ProjectCreateDTO projectCreateDTO = new ProjectCreateDTO(
      project.getName(),
      project.getCategory(),
      project.getDescription(),
      project.getBudget(),
      project.getPriority().getValue(),
      "01-01-2025",
      project.getWorkspace().getId()
    );
    ProjectResponseDTO projectResponseDTO = new ProjectResponseDTO(
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
    );
    String jsonBody = this.objectMapper.writeValueAsString(projectCreateDTO);

    when(this.projectService.create(projectCreateDTO)).thenReturn(project);
    when(this.projectMapper.toDTO(project)).thenReturn(projectResponseDTO);

    this.mockMvc.perform(post(this.baseUrl)
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.CREATED.value()))
      .andExpect(jsonPath("$.message").value("Projeto criado com sucesso"))
      .andExpect(jsonPath("$.data.id").value(projectResponseDTO.id()))
      .andExpect(jsonPath("$.data.name").value(projectResponseDTO.name()))
      .andExpect(jsonPath("$.data.priority").value(projectResponseDTO.priority()))
      .andExpect(jsonPath("$.data.category").value(projectResponseDTO.category()))
      .andExpect(jsonPath("$.data.description").value(projectResponseDTO.description()))
      .andExpect(jsonPath("$.data.budget").value(projectResponseDTO.budget()))
      .andExpect(jsonPath("$.data.deadline").value(projectResponseDTO.deadline()))
      .andExpect(jsonPath("$.data.createdAt").value(projectResponseDTO.createdAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(projectResponseDTO.updatedAt().toString()))
      .andExpect(jsonPath("$.data.ownerId").value(projectResponseDTO.ownerId()))
      .andExpect(jsonPath("$.data.workspaceId").value(projectResponseDTO.workspaceId()));

    verify(this.projectService, times(1)).create(projectCreateDTO);
    verify(this.projectMapper, times(1)).toDTO(project);
  }

  @Test
  @DisplayName("create - Should return an error response with bad request status code")
  void createProjectFailsByInvalidDate() throws Exception {
    Project project = this.dataMock.getProjects().get(1);
    ProjectCreateDTO projectDTO = new ProjectCreateDTO(
      project.getName(),
      project.getCategory(),
      project.getDescription(),
      project.getBudget(),
      project.getPriority().getValue(),
      "23-02-2024",
      this.dataMock.getWorkspaces().get(0).getId()
    );
    String jsonBody = this.objectMapper.writeValueAsString(projectDTO);

    when(this.projectService.create(projectDTO))
      .thenThrow(new InvalidDateException(
        "Data inválida. O prazo de entrega do projeto não deve ser antes da data atual" +
        "\nData atual: 24-02-2024" +
        "\nPrazo do projeto: 23-02-2024"
      ));

    this.mockMvc.perform(post(this.baseUrl)
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
      .andExpect(jsonPath("$.message").value(
        "Data inválida. O prazo de entrega do projeto não deve ser antes da data atual" +
        "\nData atual: 24-02-2024" +
        "\nPrazo do projeto: 23-02-2024"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.projectService, times(1)).create(projectDTO);
    verify(this.projectMapper, never()).toDTO(any(Project.class));
  }
}
