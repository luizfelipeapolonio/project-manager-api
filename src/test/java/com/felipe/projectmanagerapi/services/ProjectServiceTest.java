package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.ProjectCreateDTO;
import com.felipe.projectmanagerapi.dtos.ProjectUpdateDTO;
import com.felipe.projectmanagerapi.dtos.mappers.ProjectMapper;
import com.felipe.projectmanagerapi.enums.PriorityLevel;
import com.felipe.projectmanagerapi.exceptions.InvalidDateException;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.infra.security.AuthorizationService;
import com.felipe.projectmanagerapi.infra.security.UserPrincipal;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.repositories.ProjectRepository;
import com.felipe.projectmanagerapi.utils.ConvertDateFormat;
import com.felipe.projectmanagerapi.utils.GenerateMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

public class ProjectServiceTest {

  @Autowired
  @InjectMocks
  ProjectService projectService;

  @Spy
  ProjectMapper projectMapper;

  @Mock
  ProjectRepository projectRepository;

  @Mock
  AuthorizationService authorizationService;

  @Mock
  WorkspaceService workspaceService;

  @Mock
  UserService userService;

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
  @DisplayName("create - Should successfully create a project and return it")
  void createProjectSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    Project project = this.dataMock.getProjects().get(1);
    Workspace workspace = this.dataMock.getWorkspaces().get(0);

    ProjectCreateDTO projectDTO = new ProjectCreateDTO(
      project.getName(),
      project.getCategory(),
      project.getDescription(),
      project.getBudget(),
      project.getPriority().getValue(),
      "01-01-2025",
      workspace.getId()
    );

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceService.getById(anyString())).thenReturn(workspace);
    when(this.projectRepository.save(any(Project.class))).thenReturn(project);

    Project createdProject = this.projectService.create(projectDTO);

    assertThat(createdProject.getId()).isEqualTo(project.getId());
    assertThat(createdProject.getName()).isEqualTo(project.getName());
    assertThat(createdProject.getPriority()).isEqualTo(project.getPriority());
    assertThat(createdProject.getCategory()).isEqualTo(project.getCategory());
    assertThat(createdProject.getDescription()).isEqualTo(project.getDescription());
    assertThat(createdProject.getBudget()).isEqualTo(project.getBudget());
    assertThat(createdProject.getDeadline()).isEqualTo(project.getDeadline());
    assertThat(createdProject.getCreatedAt()).isEqualTo(project.getCreatedAt());
    assertThat(createdProject.getUpdatedAt()).isEqualTo(project.getUpdatedAt());
    assertThat(createdProject.getOwner().getId()).isEqualTo(userPrincipal.getUser().getId());
    assertThat(createdProject.getWorkspace().getId()).isEqualTo(workspace.getId());

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceService, times(1)).getById(anyString());
    verify(this.projectMapper, times(1)).convertValueToPriorityLevel(anyString());
    verify(this.projectRepository, times(1)).save(any(Project.class));
  }

  @Test
  @DisplayName("create - Should throw an InvalidDateException if the deadline date is invalid")
  void createProjectFailsByInvalidDeadlineDate() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    Project project = this.dataMock.getProjects().get(1);
    Workspace workspace = this.dataMock.getWorkspaces().get(0);

    ProjectCreateDTO projectDTO = new ProjectCreateDTO(
      project.getName(),
      project.getCategory(),
      project.getDescription(),
      project.getBudget(),
      project.getPriority().getValue(),
      "21-02-2024",
      workspace.getId()
    );
    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceService.getById(anyString())).thenReturn(workspace);

    Exception thrown = catchException(() -> this.projectService.create(projectDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(InvalidDateException.class)
      .hasMessage(
        "Data inválida. O prazo de entrega do projeto não deve ser antes da data atual" +
        "\nData atual: " + today +
        "\nPrazo do projeto: 21-02-2024"
      );

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceService, times(1)).getById(anyString());
    verify(this.projectRepository, never()).save(any(Project.class));
  }

  @Test
  @DisplayName("update - Should successfully update the project and return it")
  void updateProjectSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    Project project = this.dataMock.getProjects().get(1);

    ProjectUpdateDTO projectUpdateDTO = new ProjectUpdateDTO(
      "Updated Project",
      "Infra",
      "Updated description",
      new BigDecimal("2500.00"),
      "media",
      "27-10-2025"
    );
    Project updatedProjectEntity = new Project();
    updatedProjectEntity.setId(project.getId());
    updatedProjectEntity.setName(projectUpdateDTO.name());
    updatedProjectEntity.setCategory(projectUpdateDTO.category());
    updatedProjectEntity.setDescription(projectUpdateDTO.description());
    updatedProjectEntity.setBudget(projectUpdateDTO.budget());
    updatedProjectEntity.setPriority(PriorityLevel.MEDIUM);
    updatedProjectEntity.setDeadline(ConvertDateFormat.convertFormattedStringToDate(projectUpdateDTO.deadline()));
    updatedProjectEntity.setCreatedAt(project.getCreatedAt());
    updatedProjectEntity.setUpdatedAt(project.getUpdatedAt());
    updatedProjectEntity.setOwner(project.getOwner());
    updatedProjectEntity.setWorkspace(project.getWorkspace());

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.projectRepository.findById("02")).thenReturn(Optional.of(project));
    when(this.projectRepository.save(project)).thenReturn(project);

    Project updatedProject = this.projectService.update("02", projectUpdateDTO);

    assertThat(updatedProject.getId()).isEqualTo(updatedProjectEntity.getId());
    assertThat(updatedProject.getName()).isEqualTo(updatedProjectEntity.getName());
    assertThat(updatedProject.getCategory()).isEqualTo(updatedProjectEntity.getCategory());
    assertThat(updatedProject.getPriority()).isEqualTo(updatedProjectEntity.getPriority());
    assertThat(updatedProject.getDescription()).isEqualTo(updatedProjectEntity.getDescription());
    assertThat(updatedProject.getBudget()).isEqualTo(updatedProjectEntity.getBudget());
    assertThat(updatedProject.getDeadline()).isEqualTo(updatedProjectEntity.getDeadline());
    assertThat(updatedProject.getCreatedAt()).isEqualTo(updatedProjectEntity.getCreatedAt());
    assertThat(updatedProject.getUpdatedAt()).isEqualTo(updatedProjectEntity.getUpdatedAt());
    assertThat(updatedProject.getOwner().getId()).isEqualTo(updatedProjectEntity.getOwner().getId());
    assertThat(updatedProject.getWorkspace().getId()).isEqualTo(updatedProjectEntity.getWorkspace().getId());

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.projectRepository, times(1)).findById("02");
    verify(this.projectRepository, times(1)).save(project);
  }

  @Test
  @DisplayName("update - Should throw a RecordNotFoundException if the project is not found")
  void updateProjectFailsByProjectNotFound() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    Project projectMock = this.dataMock.getProjects().get(1);
    ProjectUpdateDTO projectDTO = new ProjectUpdateDTO(
      projectMock.getName(),
      projectMock.getCategory(),
      projectMock.getDescription(),
      projectMock.getBudget(),
      projectMock.getPriority().getValue(),
      "01-01-2025"
    );

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.projectRepository.findById("02")).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.projectService.update("02", projectDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Projeto de ID: '02' não encontrado");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.projectRepository, times(1)).findById("02");
    verify(this.projectRepository, never()).save(any(Project.class));
  }

  @Test
  @DisplayName("update - Should throw an AccessDeniedException if the project owner id is different from authenticated user id")
  void updateProjectFailsByDifferentOwnerId() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));
    Project project = this.dataMock.getProjects().get(1);
    ProjectUpdateDTO projectDTO = new ProjectUpdateDTO(
      project.getName(),
      project.getCategory(),
      project.getDescription(),
      project.getBudget(),
      project.getPriority().getValue(),
      "01-01-2025"
    );

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.projectRepository.findById("02")).thenReturn(Optional.of(project));

    Exception thrown = catchException(() -> this.projectService.update("02", projectDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para alterar este recurso");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.projectRepository, times(1)).findById("02");
    verify(this.projectRepository, never()).save(any(Project.class));
  }

  @Test
  @DisplayName("update - Should throw an InvalidDateException if the deadline is before current date")
  void updateProjectFailsByInvalidDeadlineDate() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    Project project = this.dataMock.getProjects().get(1);
    ProjectUpdateDTO projectDTO = new ProjectUpdateDTO(
      project.getName(),
      project.getCategory(),
      project.getDescription(),
      project.getBudget(),
      project.getPriority().getValue(),
      "26-02-2024"
    );
    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.projectRepository.findById("02")).thenReturn(Optional.of(project));

    Exception thrown = catchException(() -> this.projectService.update("02", projectDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(InvalidDateException.class)
      .hasMessage(
        "Data inválida. O prazo de entrega do projeto não deve ser antes da data atual" +
        "\nData atual: " + today +
        "\nPrazo do projeto: 26-02-2024"
      );

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.projectRepository, times(1)).findById("02");
    verify(this.projectRepository, never()).save(any(Project.class));
  }

  @Test
  @DisplayName("getAllByWorkspaceAndOwner - Should successfully return all the projects for a specific user in a specific workspace")
  void getAllByWorkspaceAndOwnerSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    User projectsOwner = this.dataMock.getUsers().get(1);
    List<Project> projects = List.of(this.dataMock.getProjects().get(1), this.dataMock.getProjects().get(2));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceService.getById("01")).thenReturn(workspace);
    when(this.userService.getProfile("02")).thenReturn(projectsOwner);
    when(this.projectRepository.findAllByWorkspaceIdAndOwnerId("01", "02")).thenReturn(projects);

    List<Project> foundProjects = this.projectService.getAllByWorkspaceAndOwner("01", "02");

    assertThat(foundProjects).allSatisfy(project -> {
      assertThat(project.getOwner().getId()).isEqualTo(projectsOwner.getId());
      assertThat(project.getWorkspace().getId()).isEqualTo(workspace.getId());
    }).hasSize(2);

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceService, times(1)).getById("01");
    verify(this.userService, times(1)).getProfile("02");
    verify(this.projectRepository, times(1)).findAllByWorkspaceIdAndOwnerId("01", "02");
  }

  @Test
  @DisplayName("getAllByWorkspaceAndOwner - Should throw an AccessDeniedException if the workspace owner id is different from authenticated user id")
  void getAllByWorkspaceAndOwnerFailsByDifferentWorkspaceOwnerId() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    Workspace workspace = this.dataMock.getWorkspaces().get(0);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceService.getById("01")).thenReturn(workspace);

    Exception thrown = catchException(() -> this.projectService.getAllByWorkspaceAndOwner("01", "02"));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para acessar este recurso");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceService, times(1)).getById("01");
    verify(this.projectRepository, never()).findAllByWorkspaceIdAndOwnerId(eq("01"), anyString());
  }

  @Test
  @DisplayName("deleteAllFromOwnerAndWorkspace - Should successfully delete all projects of the specific workspace and owner")
  void deleteAllFromOwnerAndWorkspaceSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));
    User projectsOwner = this.dataMock.getUsers().get(1);
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    List<Project> projects = List.of(this.dataMock.getProjects().get(1), this.dataMock.getProjects().get(2));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceService.getById("01")).thenReturn(workspace);
    when(this.userService.getProfile("02")).thenReturn(projectsOwner);
    when(this.projectRepository.findAllByWorkspaceIdAndOwnerId("01", "02")).thenReturn(projects);
    doNothing().when(this.projectRepository).deleteAll(projects);

    this.projectService.deleteAllFromOwnerAndWorkspace("01", "02");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceService, times(1)).getById("01");
    verify(this.userService, times(1)).getProfile("02");
    verify(this.projectRepository, times(1)).findAllByWorkspaceIdAndOwnerId("01", "02");
    verify(this.projectRepository, times(1)).deleteAll(any());
  }

  @Test
  @DisplayName("getById - Should successfully get a project in a workspace")
  void getByIdSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(2));
    Project project = this.dataMock.getProjects().get(0);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.projectRepository.findById("01")).thenReturn(Optional.of(project));

    Project foundProject = this.projectService.getById("01");

    assertThat(foundProject.getId()).isEqualTo(project.getId());
    assertThat(foundProject.getName()).isEqualTo(project.getName());
    assertThat(foundProject.getCategory()).isEqualTo(project.getCategory());
    assertThat(foundProject.getPriority()).isEqualTo(project.getPriority());
    assertThat(foundProject.getDescription()).isEqualTo(project.getDescription());
    assertThat(foundProject.getBudget()).isEqualTo(project.getBudget());
    assertThat(foundProject.getDeadline()).isEqualTo(project.getDeadline());
    assertThat(foundProject.getCreatedAt()).isEqualTo(project.getCreatedAt());
    assertThat(foundProject.getUpdatedAt()).isEqualTo(project.getUpdatedAt());
    assertThat(foundProject.getOwner().getId()).isEqualTo(project.getOwner().getId());
    assertThat(foundProject.getWorkspace().getId()).isEqualTo(project.getWorkspace().getId());

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.projectRepository, times(1)).findById("01");
  }

  @Test
  @DisplayName("getById - Should throw a RecordNotFoundException if the project is not found")
  void getByIdFailsByProjectNotFound() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(2));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.projectRepository.findById("01")).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.projectService.getById("01"));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Projeto de ID: '01' não encontrado");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.projectRepository, times(1)).findById("01");
  }

  @Test
  @DisplayName("getById - Should throw an AccessDeniedException if the authenticated user is not the owner or member of the workspace")
  void getByIdFailsByNotBeingOwnerOrMemberOfWorkspace() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(2));
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    workspace.setMembers(List.of(this.dataMock.getUsers().get(1)));

    Project project = this.dataMock.getProjects().get(0);
    project.setWorkspace(workspace);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.projectRepository.findById("01")).thenReturn(Optional.of(project));

    Exception thrown = catchException(() -> this.projectService.getById("01"));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para acessar este recurso");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.projectRepository, times(1)).findById("01");
  }
}
