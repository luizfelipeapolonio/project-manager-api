package com.felipe.projectmanagerapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipe.projectmanagerapi.dtos.TaskCreateDTO;
import com.felipe.projectmanagerapi.dtos.TaskResponseDTO;
import com.felipe.projectmanagerapi.dtos.mappers.TaskMapper;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.models.Task;
import com.felipe.projectmanagerapi.services.TaskService;
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
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "test")
public class TaskControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  TaskService taskService;

  @SpyBean
  TaskMapper taskMapper;

  private AutoCloseable closeable;
  private GenerateMocks dataMock;
  private final String BASE_URL = "/api/tasks";

  @BeforeEach
  void setUp() {
    this.closeable = MockitoAnnotations.openMocks(this);
    this.dataMock = new GenerateMocks();
  }

  @AfterEach
  void tearDown() throws Exception {
    this.closeable.close();
  }

  @Test
  @DisplayName("create - Should return a success response with created status code and the created task")
  void createSuccess() throws Exception {
    Task task = this.dataMock.getTasks().get(0);
    TaskCreateDTO taskCreateDTO = new TaskCreateDTO(
      task.getName(),
      task.getDescription(),
      "1200.00",
      task.getProject().getId()
    );
    TaskResponseDTO taskResponseDTO = new TaskResponseDTO(
      task.getId(),
      task.getName(),
      task.getDescription(),
      task.getCost().toString(),
      task.getCreatedAt(),
      task.getUpdatedAt(),
      task.getProject().getId(),
      task.getOwner().getId()
    );
    String jsonBody = this.objectMapper.writeValueAsString(taskCreateDTO);

    when(this.taskService.create(taskCreateDTO)).thenReturn(task);
    when(this.taskMapper.toDTO(task)).thenReturn(taskResponseDTO);

    this.mockMvc.perform(post(BASE_URL)
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.CREATED.value()))
      .andExpect(jsonPath("$.message").value("Task criada com sucesso"))
      .andExpect(jsonPath("$.data.id").value(taskResponseDTO.id()))
      .andExpect(jsonPath("$.data.name").value(taskResponseDTO.name()))
      .andExpect(jsonPath("$.data.description").value(taskResponseDTO.description()))
      .andExpect(jsonPath("$.data.cost").value(taskResponseDTO.cost()))
      .andExpect(jsonPath("$.data.createdAt").value(taskResponseDTO.createdAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(taskResponseDTO.updatedAt().toString()))
      .andExpect(jsonPath("$.data.projectId").value(taskResponseDTO.projectId()))
      .andExpect(jsonPath("$.data.ownerId").value(taskResponseDTO.ownerId()));

    verify(this.taskService, times(1)).create(taskCreateDTO);
    verify(this.taskMapper, times(1)).toDTO(task);
  }

  @Test
  @DisplayName("getById - Should return a success response with OK status code and the task")
  void getByIdSuccess() throws Exception {
    Task task = this.dataMock.getTasks().get(0);
    TaskResponseDTO taskResponseDTO = new TaskResponseDTO(
      task.getId(),
      task.getName(),
      task.getDescription(),
      task.getCost().toString(),
      task.getCreatedAt(),
      task.getUpdatedAt(),
      task.getProject().getId(),
      task.getOwner().getId()
    );

    when(this.taskService.getById("01")).thenReturn(task);
    when(this.taskMapper.toDTO(task)).thenReturn(taskResponseDTO);

    this.mockMvc.perform(get(BASE_URL + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Task encontrada"))
      .andExpect(jsonPath("$.data.id").value(taskResponseDTO.id()))
      .andExpect(jsonPath("$.data.name").value(taskResponseDTO.name()))
      .andExpect(jsonPath("$.data.description").value(taskResponseDTO.description()))
      .andExpect(jsonPath("$.data.cost").value(taskResponseDTO.cost()))
      .andExpect(jsonPath("$.data.createdAt").value(taskResponseDTO.createdAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(taskResponseDTO.updatedAt().toString()))
      .andExpect(jsonPath("$.data.projectId").value(taskResponseDTO.projectId()))
      .andExpect(jsonPath("$.data.ownerId").value(taskResponseDTO.ownerId()));

    verify(this.taskService, times(1)).getById("01");
    verify(this.taskMapper, times(1)).toDTO(task);
  }

  @Test
  @DisplayName("getById - Should return an error response with not found status code")
  void getByIdFailsByTaskNotFound() throws Exception {
    when(this.taskService.getById("01"))
      .thenThrow(new RecordNotFoundException("Task de ID: '01' não encontrada"));

    this.mockMvc.perform(get(BASE_URL + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Task de ID: '01' não encontrada"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.taskService, times(1)).getById("01");
    verify(this.taskMapper, never()).toDTO(any(Task.class));
  }

  @Test
  @DisplayName("getById - Should return an error response with forbidden status code")
  void getByIdFailsByNotBeingWorkspaceOwnerOrMember() throws Exception {
    when(this.taskService.getById("01"))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para acessar este recurso"));

    this.mockMvc.perform(get(BASE_URL + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para acessar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.taskService, times(1)).getById("01");
    verify(this.taskMapper, never()).toDTO(any(Task.class));
  }

  @Test
  @DisplayName("delete - Should return a success response with OK status code and the deleted task")
  void deleteSuccess() throws Exception {
    Task task = this.dataMock.getTasks().get(0);
    TaskResponseDTO taskResponseDTO = new TaskResponseDTO(
      task.getId(),
      task.getName(),
      task.getDescription(),
      task.getCost().toString(),
      task.getCreatedAt(),
      task.getUpdatedAt(),
      task.getProject().getId(),
      task.getOwner().getId()
    );

    Map<String, TaskResponseDTO> taskResponseMap = new HashMap<>(1);
    taskResponseMap.put("deletedTask", taskResponseDTO);

    when(this.taskService.delete("01")).thenReturn(task);
    when(this.taskMapper.toDTO(task)).thenReturn(taskResponseDTO);

    this.mockMvc.perform(delete(BASE_URL + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Task excluída com sucesso"))
      .andExpect(jsonPath("$.data.deletedTask.id").value(taskResponseMap.get("deletedTask").id()))
      .andExpect(jsonPath("$.data.deletedTask.name").value(taskResponseMap.get("deletedTask").name()))
      .andExpect(jsonPath("$.data.deletedTask.description").value(taskResponseMap.get("deletedTask").description()))
      .andExpect(jsonPath("$.data.deletedTask.cost").value(taskResponseMap.get("deletedTask").cost()))
      .andExpect(jsonPath("$.data.deletedTask.createdAt").value(taskResponseMap.get("deletedTask").createdAt().toString()))
      .andExpect(jsonPath("$.data.deletedTask.updatedAt").value(taskResponseMap.get("deletedTask").updatedAt().toString()))
      .andExpect(jsonPath("$.data.deletedTask.projectId").value(taskResponseMap.get("deletedTask").projectId()))
      .andExpect(jsonPath("$.data.deletedTask.ownerId").value(taskResponseMap.get("deletedTask").ownerId()));

    verify(this.taskService, times(1)).delete("01");
    verify(this.taskMapper, times(1)).toDTO(task);
  }

  @Test
  @DisplayName("delete - Should return an error response with forbidden status code")
  void deleteFailsByAccessDenied() throws Exception {
    when(this.taskService.delete("01"))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para remover este recurso"));

    this.mockMvc.perform(delete(BASE_URL + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para remover este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.taskService, times(1)).delete("01");
    verify(this.taskMapper, never()).toDTO(any(Task.class));
  }

  @Test
  @DisplayName("delete - Should return en error response with not found status code")
  void deleteFailsByTaskNotFound() throws Exception {
    when(this.taskService.delete("01"))
      .thenThrow(new RecordNotFoundException("Task de ID: '01' não encontrada"));

    this.mockMvc.perform(delete(BASE_URL + "/01")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Task de ID: '01' não encontrada"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.taskService, times(1)).delete("01");
    verify(this.taskMapper, never()).toDTO(any(Task.class));
  }
}
