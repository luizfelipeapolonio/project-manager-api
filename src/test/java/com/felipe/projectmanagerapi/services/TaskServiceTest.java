package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.TaskCreateDTO;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.infra.security.AuthorizationService;
import com.felipe.projectmanagerapi.infra.security.UserPrincipal;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.models.Task;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.repositories.TaskRepository;
import com.felipe.projectmanagerapi.utils.GenerateMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

public class TaskServiceTest {

  @Autowired
  @InjectMocks
  TaskService taskService;

  @Mock
  TaskRepository taskRepository;

  @Mock
  AuthorizationService authorizationService;

  @Mock
  ProjectService projectService;

  @Mock
  Authentication authentication;

  private AutoCloseable closeable;
  private GenerateMocks dataMock;

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
  @DisplayName("create - Should successfully create a task")
  void createSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    Project project = this.dataMock.getProjects().get(1);
    Task task = this.dataMock.getTasks().get(0);

    TaskCreateDTO taskCreateDTO = new TaskCreateDTO(
      task.getName(),
      task.getDescription(),
      "1200.00",
      project.getId()
    );

    BigDecimal cost = new BigDecimal(taskCreateDTO.cost()).setScale(2, RoundingMode.FLOOR);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.projectService.getById("02")).thenReturn(project);
    doNothing().when(this.projectService).addCost(project, cost);
    when(this.taskRepository.save(any(Task.class))).thenReturn(task);

    Task createdTask = this.taskService.create(taskCreateDTO);

    assertThat(createdTask.getId()).isEqualTo(task.getId());
    assertThat(createdTask.getName()).isEqualTo(task.getName());
    assertThat(createdTask.getDescription()).isEqualTo(task.getDescription());
    assertThat(createdTask.getCost()).isEqualTo(task.getCost());
    assertThat(createdTask.getCreatedAt()).isEqualTo(task.getCreatedAt());
    assertThat(createdTask.getUpdatedAt()).isEqualTo(task.getUpdatedAt());
    assertThat(createdTask.getProject().getId()).isEqualTo(project.getId());
    assertThat(createdTask.getOwner().getId()).isEqualTo(userPrincipal.getUser().getId());

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.projectService, times(1)).getById("02");
    verify(this.projectService, times(1)).addCost(project, cost);
    verify(this.taskRepository, times(1)).save(any(Task.class));
  }

  @Test
  @DisplayName("getById - Should successfully get a task of a project")
  void getByIdSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(2));
    Task task = this.dataMock.getTasks().get(0);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.taskRepository.findById("01")).thenReturn(Optional.of(task));

    Task foundTask = this.taskService.getById("01");

    assertThat(foundTask.getId()).isEqualTo(task.getId());
    assertThat(foundTask.getName()).isEqualTo(task.getName());
    assertThat(foundTask.getDescription()).isEqualTo(task.getDescription());
    assertThat(foundTask.getCost()).isEqualTo(task.getCost());
    assertThat(foundTask.getCreatedAt()).isEqualTo(task.getCreatedAt());
    assertThat(foundTask.getUpdatedAt()).isEqualTo(task.getUpdatedAt());
    assertThat(foundTask.getProject().getId()).isEqualTo(task.getProject().getId());
    assertThat(foundTask.getOwner().getId()).isEqualTo(task.getOwner().getId());

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.taskRepository, times(1)).findById("01");
  }

  @Test
  @DisplayName("getById - Should throw a RecordNotFoundException if the task is not found")
  void getByIdFailsByTaskNotFound() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(2));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.taskRepository.findById("01")).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.taskService.getById("01"));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Task de ID: '01' não encontrada");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.taskRepository, times(1)).findById("01");
  }

  @Test
  @DisplayName("getById - Should throw an AccessDeniedException if the authenticated user is not the workspace owner or member")
  void getByIdFailsByNotBeingWorkspaceOwnerOrMember() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(2));
    Task task = this.dataMock.getTasks().get(0);

    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    workspace.setMembers(List.of(this.dataMock.getUsers().get(1)));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.taskRepository.findById("01")).thenReturn(Optional.of(task));

    Exception thrown = catchException(() -> this.taskService.getById("01"));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para acessar este recurso");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.taskRepository, times(1)).findById("01");
  }
}
