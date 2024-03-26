package com.felipe.projectmanagerapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipe.projectmanagerapi.dtos.*;
import com.felipe.projectmanagerapi.dtos.mappers.ProjectMapper;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.exceptions.InvalidDateException;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.services.ProjectService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;

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
      project.getCost().toString(),
      ConvertDateFormat.convertDateToFormattedString(project.getDeadline()),
      project.getCreatedAt(),
      project.getUpdatedAt(),
      project.getOwner().getId(),
      project.getWorkspace().getId()
    );
    String jsonBody = this.objectMapper.writeValueAsString(projectCreateDTO);

    when(this.projectService.create(projectCreateDTO)).thenReturn(project);
    when(this.projectMapper.toProjectResponseDTO(project)).thenReturn(projectResponseDTO);

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
      .andExpect(jsonPath("$.data.cost").value(projectResponseDTO.cost()))
      .andExpect(jsonPath("$.data.deadline").value(projectResponseDTO.deadline()))
      .andExpect(jsonPath("$.data.createdAt").value(projectResponseDTO.createdAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(projectResponseDTO.updatedAt().toString()))
      .andExpect(jsonPath("$.data.ownerId").value(projectResponseDTO.ownerId()))
      .andExpect(jsonPath("$.data.workspaceId").value(projectResponseDTO.workspaceId()));

    verify(this.projectService, times(1)).create(projectCreateDTO);
    verify(this.projectMapper, times(1)).toProjectResponseDTO(project);
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
    verify(this.projectMapper, never()).toProjectResponseDTO(any(Project.class));
  }

  @Test
  @DisplayName("update - Should return a success response with OK status code")
  void updateProjectSuccess() throws Exception {
    Project project = this.dataMock.getProjects().get(1);
    ProjectUpdateDTO projectDTO = new ProjectUpdateDTO(
      project.getName(),
      project.getCategory(),
      project.getDescription(),
      project.getBudget(),
      project.getPriority().getValue(),
      "27-02-2024"
    );
    ProjectResponseDTO projectResponseDTO = new ProjectResponseDTO(
      project.getId(),
      project.getName(),
      project.getPriority().getValue(),
      project.getCategory(),
      project.getDescription(),
      project.getBudget().toString(),
      project.getCost().toString(),
      ConvertDateFormat.convertDateToFormattedString(project.getDeadline()),
      project.getCreatedAt(),
      project.getUpdatedAt(),
      project.getOwner().getId(),
      project.getWorkspace().getId()
    );
    String jsonBody = this.objectMapper.writeValueAsString(projectDTO);

    when(this.projectService.update("02", projectDTO)).thenReturn(project);
    when(this.projectMapper.toProjectResponseDTO(project)).thenReturn(projectResponseDTO);

    this.mockMvc.perform(patch(this.baseUrl + "/02")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Projeto atualizado com sucesso"))
      .andExpect(jsonPath("$.data.id").value(projectResponseDTO.id()))
      .andExpect(jsonPath("$.data.name").value(projectResponseDTO.name()))
      .andExpect(jsonPath("$.data.priority").value(projectResponseDTO.priority()))
      .andExpect(jsonPath("$.data.category").value(projectResponseDTO.category()))
      .andExpect(jsonPath("$.data.description").value(projectResponseDTO.description()))
      .andExpect(jsonPath("$.data.budget").value(projectResponseDTO.budget()))
      .andExpect(jsonPath("$.data.cost").value(projectResponseDTO.cost()))
      .andExpect(jsonPath("$.data.deadline").value(projectResponseDTO.deadline()))
      .andExpect(jsonPath("$.data.createdAt").value(projectResponseDTO.createdAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(projectResponseDTO.updatedAt().toString()))
      .andExpect(jsonPath("$.data.ownerId").value(projectResponseDTO.ownerId()))
      .andExpect(jsonPath("$.data.workspaceId").value(projectResponseDTO.workspaceId()));

    verify(this.projectService, times(1)).update("02", projectDTO);
    verify(this.projectMapper, times(1)).toProjectResponseDTO(project);
  }

  @Test
  @DisplayName("update - Should return an error response with not found status code")
  void updateProjectFailsByProjectNotFound() throws Exception {
    Project project = this.dataMock.getProjects().get(1);
    ProjectUpdateDTO projectDTO = new ProjectUpdateDTO(
      project.getName(),
      project.getCategory(),
      project.getDescription(),
      project.getBudget(),
      project.getPriority().getValue(),
      "01-01-2025"
    );
    String jsonBody = this.objectMapper.writeValueAsString(projectDTO);

    when(this.projectService.update("02", projectDTO))
      .thenThrow(new RecordNotFoundException("Projeto de ID: '02' não encontrado"));

    this.mockMvc.perform(patch(this.baseUrl + "/02")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Projeto de ID: '02' não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.projectService, times(1)).update("02", projectDTO);
    verify(this.projectMapper, never()).toProjectResponseDTO(any(Project.class));
  }

  @Test
  @DisplayName("update - Should return an error response with forbidden access code")
  void updateProjectFailsByDifferentOwnerId() throws Exception {
    Project project = this.dataMock.getProjects().get(1);
    ProjectUpdateDTO projectDTO = new ProjectUpdateDTO(
      project.getName(),
      project.getCategory(),
      project.getDescription(),
      project.getBudget(),
      project.getPriority().getValue(),
      "01-01-2025"
    );
    String jsonBody = this.objectMapper.writeValueAsString(projectDTO);

    when(this.projectService.update("02", projectDTO))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para alterar este recurso"));

    this.mockMvc.perform(patch(this.baseUrl + "/02")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para alterar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.projectService, times(1)).update("02", projectDTO);
    verify(this.projectMapper, never()).toProjectResponseDTO(any(Project.class));
  }

  @Test
  @DisplayName("update - Should return an error response with bad request status code")
  void updateProjectFailsByInvalidDeadlineDate() throws Exception {
    Project project = this.dataMock.getProjects().get(1);
    ProjectUpdateDTO projectDTO = new ProjectUpdateDTO(
      project.getName(),
      project.getCategory(),
      project.getDescription(),
      project.getBudget(),
      project.getPriority().getValue(),
      "01-01-2025"
    );
    String jsonBody = this.objectMapper.writeValueAsString(projectDTO);

    when(this.projectService.update("02", projectDTO))
      .thenThrow(new InvalidDateException(
        "Data inválida. O prazo de entrega do projeto não deve ser antes da data atual" +
        "\nData atual: 24-02-2024" +
        "\nPrazo do projeto: 23-02-2024"
      ));

    this.mockMvc.perform(patch(this.baseUrl + "/02")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
      .andExpect(jsonPath("$.message").value(
        "Data inválida. O prazo de entrega do projeto não deve ser antes da data atual" +
        "\nData atual: 24-02-2024" +
        "\nPrazo do projeto: 23-02-2024"
      ))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.projectService, times(1)).update("02", projectDTO);
    verify(this.projectMapper, never()).toProjectResponseDTO(any(Project.class));
  }

  @Test
  @DisplayName("getAllByWorkspaceAndOwner - Should return a success response with OK status code and a list of project DTOs")
  void getAllByWorkspaceAndOwnerSuccess() throws Exception {
    List<Project> projects = List.of(this.dataMock.getProjects().get(1), this.dataMock.getProjects().get(2));

    List<ProjectResponseDTO> projectDTOs = projects.stream()
      .map(project -> new ProjectResponseDTO(
        project.getId(),
        project.getName(),
        project.getPriority().getValue(),
        project.getCategory(),
        project.getDescription(),
        project.getBudget().toString(),
        project.getCost().toString(),
        ConvertDateFormat.convertDateToFormattedString(project.getDeadline()),
        project.getCreatedAt(),
        project.getUpdatedAt(),
        project.getOwner().getId(),
        project.getWorkspace().getId()
      ))
      .toList();

    CustomResponseBody<List<ProjectResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os projetos do usuário de id '02' no workspace de id '01'");
    response.setData(projectDTOs);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.projectService.getAllByWorkspaceAndOwner("01", "02")).thenReturn(projects);

    this.mockMvc.perform(get(this.baseUrl + "/workspaces/01/owner/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.projectService, times(1)).getAllByWorkspaceAndOwner("01", "02");
    verify(this.projectMapper, times(2)).toProjectResponseDTO(any(Project.class));
  }

  @Test
  @DisplayName("getAllByWorkspaceAndOwner - Should return an error response with forbidden status code")
  void getAllByWorkspaceAndOwnerFailsByDifferentWorkspaceOwner() throws Exception {
    when(this.projectService.getAllByWorkspaceAndOwner("01", "02"))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para acessar este recurso"));

    this.mockMvc.perform(get(this.baseUrl + "/workspaces/01/owner/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para acessar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.projectService, times(1)).getAllByWorkspaceAndOwner("01", "02");
    verify(this.projectMapper, never()).toProjectResponseDTO(any(Project.class));
  }

  @Test
  @DisplayName("getById - Should return a success response with OK status code and the found project")
  void getByIdSuccess() throws Exception {
    Project project = this.dataMock.getProjects().get(0);
    project.setTasks(this.dataMock.getTasks());

    ProjectResponseDTO projectResponseDTO = new ProjectResponseDTO(
      project.getId(),
      project.getName(),
      project.getPriority().getValue(),
      project.getCategory(),
      project.getDescription(),
      project.getBudget().toString(),
      project.getCost().toString(),
      ConvertDateFormat.convertDateToFormattedString(project.getDeadline()),
      project.getCreatedAt(),
      project.getUpdatedAt(),
      project.getOwner().getId(),
      project.getWorkspace().getId()
    );
    List<TaskResponseDTO> taskResponseDTOs = project.getTasks()
      .stream()
      .map(task -> new TaskResponseDTO(
        task.getId(),
        task.getName(),
        task.getDescription(),
        task.getCost().toString(),
        task.getCreatedAt(),
        task.getUpdatedAt(),
        task.getProject().getId(),
        task.getOwner().getId()
      ))
      .toList();

    ProjectFullResponseDTO projectFullResponseDTO = new ProjectFullResponseDTO(projectResponseDTO, taskResponseDTOs);

    CustomResponseBody<ProjectFullResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Projeto encontrado");
    response.setData(projectFullResponseDTO);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.projectService.getById("01")).thenReturn(project);

    this.mockMvc.perform(get(this.baseUrl + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.projectService, times(1)).getById("01");
    verify(this.projectMapper, times(1)).toProjectFullResponseDTO(project);
  }

  @Test
  @DisplayName("getById - Should return an error response with not found status code")
  void getByIdFailsByProjectNotFound() throws Exception {
    when(this.projectService.getById("01"))
      .thenThrow(new RecordNotFoundException("Projeto de ID: '01' não encontrado"));

    this.mockMvc.perform(get(this.baseUrl + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Projeto de ID: '01' não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.projectService, times(1)).getById("01");
    verify(this.projectMapper, never()).toProjectResponseDTO(any(Project.class));
  }

  @Test
  @DisplayName("getAllFromWorkspace - Should return a success response with OK status code and a list of projects")
  void getAllFromWorkspaceSuccess() throws Exception {
    List<Project> projects = this.dataMock.getProjects();
    List<ProjectResponseDTO> projectsDTO = projects.stream()
      .map(project -> new ProjectResponseDTO(
        project.getId(),
        project.getName(),
        project.getPriority().getValue(),
        project.getCategory(),
        project.getDescription(),
        project.getBudget().toString(),
        project.getCost().toString(),
        ConvertDateFormat.convertDateToFormattedString(project.getDeadline()),
        project.getCreatedAt(),
        project.getUpdatedAt(),
        project.getOwner().getId(),
        project.getWorkspace().getId()
      ))
      .toList();

    CustomResponseBody<List<ProjectResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os projetos do workspace de ID: '01'");
    response.setData(projectsDTO);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.projectService.getAllFromWorkspace(eq("01"), anyString())).thenReturn(projects);

    this.mockMvc.perform(get(this.baseUrl + "/workspaces/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.projectService, times(1)).getAllFromWorkspace(eq("01"), anyString());
    verify(this.projectMapper, times(3)).toProjectResponseDTO(any(Project.class));
  }

  @Test
  @DisplayName("getAllFromAuthenticatedUser - Should return a success response with OK status code and all user projects")
  void getAllFromAuthenticatedUserSuccess() throws Exception {
    List<Project> projects = List.of(this.dataMock.getProjects().get(1), this.dataMock.getProjects().get(2));
    List<ProjectResponseDTO> projectsDTO = projects.stream()
      .map(project -> new ProjectResponseDTO(
        project.getId(),
        project.getName(),
        project.getPriority().getValue(),
        project.getCategory(),
        project.getDescription(),
        project.getBudget().toString(),
        project.getCost().toString(),
        ConvertDateFormat.convertDateToFormattedString(project.getDeadline()),
        project.getCreatedAt(),
        project.getUpdatedAt(),
        project.getOwner().getId(),
        project.getWorkspace().getId()
      ))
      .toList();

    CustomResponseBody<List<ProjectResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os seus projetos");
    response.setData(projectsDTO);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.projectService.getAllFromAuthenticatedUser()).thenReturn(projects);

    this.mockMvc.perform(get(this.baseUrl).accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.projectService, times(1)).getAllFromAuthenticatedUser();
    verify(this.projectMapper, times(2)).toProjectResponseDTO(any(Project.class));
  }

  @Test
  @DisplayName("delete - Should return a success response with OK status and the deleted project")
  void deleteSuccess() throws Exception {
    Project project = this.dataMock.getProjects().get(0);
    ProjectResponseDTO projectResponseDTO = new ProjectResponseDTO(
      project.getId(),
      project.getName(),
      project.getPriority().getValue(),
      project.getCategory(),
      project.getDescription(),
      project.getBudget().toString(),
      project.getCost().toString(),
      ConvertDateFormat.convertDateToFormattedString(project.getDeadline()),
      project.getCreatedAt(),
      project.getUpdatedAt(),
      project.getOwner().getId(),
      project.getWorkspace().getId()
    );

    Map<String, ProjectResponseDTO> responseMap = new HashMap<>(1);
    responseMap.put("deletedProject", projectResponseDTO);

    when(this.projectService.delete("01")).thenReturn(project);
    when(this.projectMapper.toProjectResponseDTO(project)).thenReturn(projectResponseDTO);

    this.mockMvc.perform(delete(this.baseUrl + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Projeto excluído com sucesso"))
      .andExpect(jsonPath("$.data.deletedProject.id").value(responseMap.get("deletedProject").id()))
      .andExpect(jsonPath("$.data.deletedProject.name").value(responseMap.get("deletedProject").name()))
      .andExpect(jsonPath("$.data.deletedProject.priority").value(responseMap.get("deletedProject").priority()))
      .andExpect(jsonPath("$.data.deletedProject.category").value(responseMap.get("deletedProject").category()))
      .andExpect(jsonPath("$.data.deletedProject.description").value(responseMap.get("deletedProject").description()))
      .andExpect(jsonPath("$.data.deletedProject.budget").value(responseMap.get("deletedProject").budget()))
      .andExpect(jsonPath("$.data.deletedProject.cost").value(responseMap.get("deletedProject").cost()))
      .andExpect(jsonPath("$.data.deletedProject.deadline").value(responseMap.get("deletedProject").deadline()))
      .andExpect(jsonPath("$.data.deletedProject.createdAt").value(responseMap.get("deletedProject").createdAt().toString()))
      .andExpect(jsonPath("$.data.deletedProject.updatedAt").value(responseMap.get("deletedProject").updatedAt().toString()))
      .andExpect(jsonPath("$.data.deletedProject.ownerId").value(responseMap.get("deletedProject").ownerId()))
      .andExpect(jsonPath("$.data.deletedProject.workspaceId").value(responseMap.get("deletedProject").workspaceId()));

    verify(this.projectService, times(1)).delete("01");
    verify(this.projectMapper, times(1)).toProjectResponseDTO(project);
  }

  @Test
  @DisplayName("delete - Should return an error response with not found status code")
  void deleteFailsByProjectNotFound() throws Exception {
    when(this.projectService.delete("01"))
      .thenThrow(new RecordNotFoundException("Projeto de ID: '01' não encontrado"));

    this.mockMvc.perform(delete(this.baseUrl + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Projeto de ID: '01' não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.projectService, times(1)).delete("01");
    verify(this.projectMapper, never()).toProjectResponseDTO(any(Project.class));
  }

  @Test
  @DisplayName("delete - Should return an error response with forbidden status code")
  void deleteFailsByNotBeingOwnerOfWorkspaceOrProject() throws Exception {
    when(this.projectService.delete("01"))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para remover este recurso"));

    this.mockMvc.perform(delete(this.baseUrl + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para remover este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.projectService, times(1)).delete("01");
    verify(this.projectMapper, never()).toProjectResponseDTO(any(Project.class));
  }

  @Test
  @DisplayName("deleteAllFromWorkspace - Should return a success response with OK status code and the deleted projects")
  void deleteAllFromWorkspaceSuccess() throws Exception {
    List<Project> projects = this.dataMock.getProjects();
    List<ProjectResponseDTO> projectsDTO = projects.stream()
      .map(project -> new ProjectResponseDTO(
        project.getId(),
        project.getName(),
        project.getPriority().getValue(),
        project.getCategory(),
        project.getDescription(),
        project.getBudget().toString(),
        project.getCost().toString(),
        ConvertDateFormat.convertDateToFormattedString(project.getDeadline()),
        project.getCreatedAt(),
        project.getUpdatedAt(),
        project.getOwner().getId(),
        project.getWorkspace().getId()
      ))
      .toList();

    Map<String, List<ProjectResponseDTO>> deletedProjects = new HashMap<>(1);
    deletedProjects.put("deletedProjects", projectsDTO);

    CustomResponseBody<Map<String, List<ProjectResponseDTO>>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os projetos do workspace de ID: '01' excluídos com sucesso");
    response.setData(deletedProjects);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.projectService.deleteAllFromWorkspace("01")).thenReturn(projects);

    this.mockMvc.perform(delete(this.baseUrl + "/workspaces/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.projectService, times(1)).deleteAllFromWorkspace("01");
    verify(this.projectMapper, times(3)).toProjectResponseDTO(any(Project.class));
  }

  @Test
  @DisplayName("deleteAllFromWorkspace - Should return an error response with forbidden status code")
  void deleteAllFromWorkspaceFailsByNotBeingWorkspaceOwner() throws Exception {
    when(this.projectService.deleteAllFromWorkspace("01"))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para remover este recurso"));

    this.mockMvc.perform(delete(this.baseUrl + "/workspaces/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para remover este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.projectService, times(1)).deleteAllFromWorkspace("01");
    verify(this.projectMapper, never()).toProjectResponseDTO(any(Project.class));
  }

  @Test
  @DisplayName("deleteAllFromAuthenticatedUser - Should return a success response with OK status code and the deleted projects")
  void deleteAllFromAuthenticatedUserSuccess() throws Exception {
    List<Project> projects = List.of(this.dataMock.getProjects().get(1), this.dataMock.getProjects().get(2));
    List<ProjectResponseDTO> projectsDTO = projects.stream()
      .map(project -> new ProjectResponseDTO(
        project.getId(),
        project.getName(),
        project.getPriority().getValue(),
        project.getCategory(),
        project.getDescription(),
        project.getBudget().toString(),
        project.getCost().toString(),
        ConvertDateFormat.convertDateToFormattedString(project.getDeadline()),
        project.getCreatedAt(),
        project.getUpdatedAt(),
        project.getOwner().getId(),
        project.getWorkspace().getId()
      ))
      .toList();

    Map<String, List<ProjectResponseDTO>> deletedProjects = new HashMap<>(1);
    deletedProjects.put("deletedProjects", projectsDTO);

    CustomResponseBody<Map<String, List<ProjectResponseDTO>>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os seus projetos foram excluídos com sucesso");
    response.setData(deletedProjects);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.projectService.deleteAllFromAuthenticatedUser()).thenReturn(projects);

    this.mockMvc.perform(delete(this.baseUrl).accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.projectService, times(1)).deleteAllFromAuthenticatedUser();
    verify(this.projectMapper, times(2)).toProjectResponseDTO(any(Project.class));
  }

  @Test
  @DisplayName("deleteAllFromWorkspaceAndOwner - Should return a success response with OK status code and all deleted projects")
  void deleteAllFromWorkspaceAndOwnerSuccess() throws Exception {
    List<Project> projects = List.of(this.dataMock.getProjects().get(1), this.dataMock.getProjects().get(2));
    List<ProjectResponseDTO> projectsDTO = projects.stream()
      .map(project -> new ProjectResponseDTO(
        project.getId(),
        project.getName(),
        project.getPriority().getValue(),
        project.getCategory(),
        project.getDescription(),
        project.getBudget().toString(),
        project.getCost().toString(),
        ConvertDateFormat.convertDateToFormattedString(project.getDeadline()),
        project.getCreatedAt(),
        project.getUpdatedAt(),
        project.getOwner().getId(),
        project.getWorkspace().getId()
      ))
      .toList();

    Map<String, List<ProjectResponseDTO>> deletedProjects = new HashMap<>(1);
    deletedProjects.put("deletedProjects", projectsDTO);

    CustomResponseBody<Map<String, List<ProjectResponseDTO>>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os projetos do usuário de ID '02' do workspace de ID '01' foram excluídos com sucesso");
    response.setData(deletedProjects);

    String jsonResponseBody = this.objectMapper.writeValueAsString(response);

    when(this.projectService.deleteAllFromOwnerAndWorkspace("01", "02")).thenReturn(projects);

    this.mockMvc.perform(delete(this.baseUrl + "/workspaces/01/owner/02")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().json(jsonResponseBody));

    verify(this.projectService, times(1)).deleteAllFromOwnerAndWorkspace("01", "02");
    verify(this.projectMapper, times(2)).toProjectResponseDTO(any(Project.class));
  }
}
