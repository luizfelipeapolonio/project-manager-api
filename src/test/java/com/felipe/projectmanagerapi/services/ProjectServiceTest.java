package com.felipe.projectmanagerapi.services;

import com.felipe.projectmanagerapi.dtos.ProjectCreateDTO;
import com.felipe.projectmanagerapi.dtos.ProjectUpdateDTO;
import com.felipe.projectmanagerapi.dtos.mappers.ProjectMapper;
import com.felipe.projectmanagerapi.enums.PriorityLevel;
import com.felipe.projectmanagerapi.exceptions.InvalidBudgetException;
import com.felipe.projectmanagerapi.exceptions.InvalidCostException;
import com.felipe.projectmanagerapi.exceptions.InvalidDateException;
import com.felipe.projectmanagerapi.exceptions.OutOfBudgetException;
import com.felipe.projectmanagerapi.exceptions.RecordNotFoundException;
import com.felipe.projectmanagerapi.infra.security.AuthorizationService;
import com.felipe.projectmanagerapi.infra.security.UserPrincipal;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.models.Task;
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
import org.mockito.Spy;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
      project.getBudget().toString(),
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
    assertThat(createdProject.getCost()).isEqualTo(project.getCost());
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
      project.getBudget().toString(),
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
    project.setCost(new BigDecimal("1000").setScale(2, RoundingMode.FLOOR));

    ProjectUpdateDTO projectUpdateDTO = new ProjectUpdateDTO(
      "Updated Project",
      "Infra",
      "Updated description",
      "2500.00",
      "media",
      "27-10-2035"
    );
    Project updatedProjectEntity = new Project();
    updatedProjectEntity.setId(project.getId());
    updatedProjectEntity.setName(projectUpdateDTO.name());
    updatedProjectEntity.setCategory(projectUpdateDTO.category());
    updatedProjectEntity.setDescription(projectUpdateDTO.description());
    updatedProjectEntity.setBudget(new BigDecimal("2500").setScale(2, RoundingMode.FLOOR));
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
      projectMock.getBudget().toString(),
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
      project.getBudget().toString(),
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
      project.getBudget().toString(),
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
  @DisplayName("update - Should throw an InvalidBudgetException if the new budget is less than current project cost")
  void updateProjectFailsByInvalidBudget() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    Project project = this.dataMock.getProjects().get(1);
    project.setCost(new BigDecimal("1000").setScale(2, RoundingMode.FLOOR));

    String deadline = LocalDate.now().plusYears(10).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

    ProjectUpdateDTO projectDTO = new ProjectUpdateDTO(
      project.getName(),
      project.getCategory(),
      project.getDescription(),
      "800.00",
      project.getPriority().getValue(),
      deadline
    );

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.projectRepository.findById("02")).thenReturn(Optional.of(project));

    Exception thrown = catchException(() -> this.projectService.update("02", projectDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(InvalidBudgetException.class)
      .hasMessage(
        "O novo orçamento é menor do que o custo atual do projeto. " +
        "Novo orçamento: R$ 800.00" +
        " Custo atual: R$ 1000.00"
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

    List<Project> deletedProjects = this.projectService.deleteAllFromOwnerAndWorkspace("01", "02");

    assertThat(deletedProjects).allSatisfy(project -> {
      assertThat(project.getOwner().getId()).isEqualTo(projectsOwner.getId());
      assertThat(project.getWorkspace().getId()).isEqualTo(workspace.getId());
    }).hasSize(2);

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
    assertThat(foundProject.getCost()).isEqualTo(project.getCost());
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

  @Test
  @DisplayName("getAllFromWorkspace - Should successfully get all projects from a workspace")
  void getAllFromWorkspaceSuccess() {
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    workspace.setProjects(this.dataMock.getProjects());

    when(this.workspaceService.getById("01")).thenReturn(workspace);
    when(this.projectRepository.findAllByWorkspaceId(eq("01"), any(Sort.class))).thenReturn(workspace.getProjects());

    List<Project> foundProjects = this.projectService.getAllFromWorkspace("01", "ASC");

    assertThat(foundProjects)
      .allSatisfy(project -> assertThat(project.getWorkspace().getId()).isEqualTo(workspace.getId()))
      .hasSize(3);

    verify(this.workspaceService, times(1)).getById("01");
    verify(this.projectRepository, times(1)).findAllByWorkspaceId(eq("01"), any(Sort.class));
  }

  @Test
  @DisplayName("getAllFromAuthenticatedUser - Should successfully get all user projects")
  void getAllFromAuthenticatedUserSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    List<Project> projects = List.of(this.dataMock.getProjects().get(1), this.dataMock.getProjects().get(2));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.projectRepository.findAllByUserId(userPrincipal.getUser().getId())).thenReturn(projects);

    List<Project> foundProjects = this.projectService.getAllFromAuthenticatedUser();

    assertThat(foundProjects)
      .allSatisfy(project -> assertThat(project.getOwner().getId()).isEqualTo(userPrincipal.getUser().getId()))
      .hasSize(2);

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.projectRepository, times(1)).findAllByUserId(userPrincipal.getUser().getId());
  }

  @Test
  @DisplayName("getAllFromOwner - Should successfully return all user projects")
  void getAllFromOwnerSuccess() {
    User projectsOwner = this.dataMock.getUsers().get(1);
    List<Project> projects = List.of(this.dataMock.getProjects().get(1), this.dataMock.getProjects().get(2));

    when(this.userService.getProfile("02")).thenReturn(projectsOwner);
    when(this.projectRepository.findAllByUserId(projectsOwner.getId())).thenReturn(projects);

    List<Project> userProjects = this.projectService.getAllFromOwner("02");

    assertThat(userProjects)
      .allSatisfy(project -> assertThat(project.getOwner().getId()).isEqualTo(projectsOwner.getId()))
      .hasSize(2);

    verify(this.userService, times(1)).getProfile("02");
    verify(this.projectRepository, times(1)).findAllByUserId(projectsOwner.getId());
  }

  @Test
  @DisplayName("delete - Should successfully delete a project")
  void deleteSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    Project project = this.dataMock.getProjects().get(1);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceService.getById("01")).thenReturn(workspace);
    when(this.projectRepository.findById("02")).thenReturn(Optional.of(project));
    doNothing().when(this.projectRepository).deleteById("02");

    Project deletedProject = this.projectService.delete("02");

    assertThat(deletedProject.getId()).isEqualTo(project.getId());
    assertThat(deletedProject.getName()).isEqualTo(project.getName());
    assertThat(deletedProject.getCategory()).isEqualTo(project.getCategory());
    assertThat(deletedProject.getPriority()).isEqualTo(project.getPriority());
    assertThat(deletedProject.getDescription()).isEqualTo(project.getDescription());
    assertThat(deletedProject.getBudget()).isEqualTo(project.getBudget());
    assertThat(deletedProject.getCost()).isEqualTo(project.getCost());
    assertThat(deletedProject.getDeadline()).isEqualTo(project.getDeadline());
    assertThat(deletedProject.getCreatedAt()).isEqualTo(project.getCreatedAt());
    assertThat(deletedProject.getUpdatedAt()).isEqualTo(project.getUpdatedAt());
    assertThat(deletedProject.getOwner().getId()).isEqualTo(project.getOwner().getId());
    assertThat(deletedProject.getWorkspace().getId()).isEqualTo(project.getWorkspace().getId());

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceService, times(1)).getById("01");
    verify(this.projectRepository, times(1)).findById("02");
    verify(this.projectRepository, times(1)).deleteById("02");
  }

  @Test
  @DisplayName("delete - Should throw a RecordNotFoundException if the project is not found")
  void deleteFailsByProjectNotFound() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.projectRepository.findById("01")).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.projectService.delete("01"));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Projeto de ID: '01' não encontrado");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.projectRepository, times(1)).findById("01");
    verify(this.workspaceService, never()).getById(anyString());
    verify(this.projectRepository, never()).deleteById(anyString());
  }

  @Test
  @DisplayName("Should throw an AccessDeniedException if the workspace and project owner is not the authenticated user")
  void deleteFailsByNotBeingOwnerOfWorkspaceOrProject() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(2));
    Project project = this.dataMock.getProjects().get(1);
    Workspace workspace = this.dataMock.getWorkspaces().get(0);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.projectRepository.findById("02")).thenReturn(Optional.of(project));
    when(this.workspaceService.getById("01")).thenReturn(workspace);

    Exception thrown = catchException(() -> this.projectService.delete("02"));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para remover este recurso");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.projectRepository, times(1)).findById("02");
    verify(this.workspaceService, times(1)).getById("01");
    verify(this.projectRepository, never()).deleteById(anyString());
  }

  @Test
  @DisplayName("deleteAllFromWorkspace - Should successfully delete all projects from a workspace")
  void deleteAllFromWorkspaceSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(0));
    Workspace workspace = this.dataMock.getWorkspaces().get(0);
    List<Project> projects = this.dataMock.getProjects();

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceService.getById("01")).thenReturn(workspace);
    when(this.projectRepository.findAllByWorkspaceId(eq("01"), any(Sort.class))).thenReturn(projects);
    doNothing().when(this.projectRepository).deleteAll(projects);

    List<Project> deletedProjects = this.projectService.deleteAllFromWorkspace("01");

    assertThat(deletedProjects)
      .allSatisfy(project -> assertThat(project.getWorkspace().getId()).isEqualTo(workspace.getId()))
      .hasSize(3);

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceService, times(1)).getById("01");
    verify(this.projectRepository, times(1)).findAllByWorkspaceId(eq("01"), any(Sort.class));
    verify(this.projectRepository, times(1)).deleteAll(projects);
  }

  @Test
  @DisplayName("deleteAllFromWorkspace - Should throw an AccessDeniedException if the authenticated user is not the workspace owner")
  void deleteAllFromWorkspaceFailsByNotBeingWorkspaceOwner() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    Workspace workspace = this.dataMock.getWorkspaces().get(0);

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.workspaceService.getById("01")).thenReturn(workspace);

    Exception thrown = catchException(() -> this.projectService.deleteAllFromWorkspace("01"));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para remover este recurso");

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.workspaceService, times(1)).getById("01");
    verify(this.projectRepository, never()).findAllByWorkspaceId(anyString(), any(Sort.class));
    verify(this.projectRepository, never()).deleteAll(any());
  }

  @Test
  @DisplayName("deleteAllFromAuthenticatedUser - Should successfully delete all authenticated user projects")
  void deleteAllFromAuthenticatedUserSuccess() {
    UserPrincipal userPrincipal = new UserPrincipal(this.dataMock.getUsers().get(1));
    List<Project> projects = List.of(this.dataMock.getProjects().get(1), this.dataMock.getProjects().get(2));

    when(this.authorizationService.getAuthentication()).thenReturn(this.authentication);
    when(this.authentication.getPrincipal()).thenReturn(userPrincipal);
    when(this.projectRepository.findAllByUserId("02")).thenReturn(projects);
    doNothing().when(this.projectRepository).deleteAll(projects);

    List<Project> deletedProjects = this.projectService.deleteAllFromAuthenticatedUser();

    assertThat(deletedProjects)
      .allSatisfy(project -> assertThat(project.getOwner().getId()).isEqualTo(userPrincipal.getUser().getId()))
      .hasSize(2);

    verify(this.authorizationService, times(1)).getAuthentication();
    verify(this.authentication, times(1)).getPrincipal();
    verify(this.projectRepository, times(1)).findAllByUserId("02");
    verify(this.projectRepository, times(1)).deleteAll(projects);
  }

  @Test
  @DisplayName("deleteAllFromOwner - Should successfully delete all projects from an owner")
  void deleteAllFromOwnerSuccess() {
    User projectsOwner = this.dataMock.getUsers().get(1);
    List<Project> projects = List.of(this.dataMock.getProjects().get(1), this.dataMock.getProjects().get(2));

    when(this.userService.getProfile("02")).thenReturn(projectsOwner);
    when(this.projectRepository.findAllByUserId(projectsOwner.getId())).thenReturn(projects);
    doNothing().when(this.projectRepository).deleteAll(projects);

    List<Project> deletedProjects = this.projectService.deleteAllFromOwner("02");

    assertThat(deletedProjects)
      .allSatisfy(project -> assertThat(project.getOwner().getId()).isEqualTo(projectsOwner.getId()))
      .hasSize(2);

    verify(this.userService, times(1)).getProfile("02");
    verify(this.projectRepository, times(1)).findAllByUserId(projectsOwner.getId());
    verify(this.projectRepository, times(1)).deleteAll(projects);
  }

  @Test
  @DisplayName("addCost - Should successfully update the project cost adding the new cost to it")
  void addCostSuccess() {
    Project project = this.dataMock.getProjects().get(1);
    BigDecimal cost = new BigDecimal("800").setScale(2, RoundingMode.FLOOR);
    BigDecimal newCost = project.getCost().add(cost);
    ArgumentCaptor<Project> projectCapture = ArgumentCaptor.forClass(Project.class);

    when(this.projectRepository.save(projectCapture.capture())).thenReturn(any(Project.class));

    this.projectService.addCost(project, cost);

    assertThat(projectCapture.getValue().getCost()).isEqualTo(newCost);
    verify(this.projectRepository, times(1)).save(project);
  }

  @Test
  @DisplayName("addCost - Should throw an OutOfBudgetException if the cost is greater than the project budget")
  void addCostFailsByCostOutOfBudget() {
    Project project = this.dataMock.getProjects().get(1);
    BigDecimal cost = new BigDecimal("1100").setScale(2, RoundingMode.FLOOR);

    Exception thrown = catchException(() -> this.projectService.addCost(project, cost));

    assertThat(thrown)
      .isExactlyInstanceOf(OutOfBudgetException.class)
      .hasMessage(
        "Operação inválida! Custo acima do orçamento do projeto.\n" +
        "Orçamento: R$ 1000.00" + "\n" +
        "Custo: R$ 1100.00"
      );

    verify(this.projectRepository, never()).save(any(Project.class));
  }

  @Test
  @DisplayName("addCost - Should throw an InvalidCostException if the cost value is less than 0")
  void addCostFailsByNegativeCostValue() {
    Project project = this.dataMock.getProjects().get(1);
    BigDecimal cost = new BigDecimal("-1");

    Exception thrown = catchException(() -> this.projectService.addCost(project, cost));

    assertThat(thrown)
      .isExactlyInstanceOf(InvalidCostException.class)
      .hasMessage("Custo inválido! Valores negativos não são permitidos. Custo: R$ -1");

    verify(this.projectRepository, never()).save(any(Project.class));
  }

  @Test
  @DisplayName("subtractCost - Should successfully subtract the task cost value from project cost")
  void subtractCostSuccess() {
    Task task = this.dataMock.getTasks().get(0);
    task.setCost(new BigDecimal("800").setScale(2, RoundingMode.FLOOR));

    Project project = this.dataMock.getProjects().get(1);
    BigDecimal newCost = project.getCost().subtract(task.getCost());
    ArgumentCaptor<Project> projectCapture = ArgumentCaptor.forClass(Project.class);

    when(this.projectRepository.save(projectCapture.capture())).thenReturn(any(Project.class));

    this.projectService.subtractCost(project, task);

    assertThat(projectCapture.getValue().getCost()).isEqualTo(newCost);
    verify(this.projectRepository, times(1)).save(project);
  }

  @Test
  @DisplayName("updateCost - Should successfully update the project cost")
  void updateCostSuccess() {
    Project project = this.dataMock.getProjects().get(1);
    project.setBudget(new BigDecimal("2000").setScale(2, RoundingMode.FLOOR));
    project.setCost(new BigDecimal("2000").setScale(2, RoundingMode.FLOOR));

    Task task = this.dataMock.getTasks().get(0);
    BigDecimal cost = new BigDecimal("800").setScale(2, RoundingMode.FLOOR);
    BigDecimal newCost = new BigDecimal("1600").setScale(2, RoundingMode.FLOOR);
    ArgumentCaptor<Project> projectCapture = ArgumentCaptor.forClass(Project.class);

    when(this.projectRepository.save(projectCapture.capture())).thenReturn(any(Project.class));

    this.projectService.updateCost(project, task, cost);

    assertThat(projectCapture.getValue().getCost()).isEqualTo(newCost);
    verify(this.projectRepository, times(1)).save(project);
  }

  @Test
  @DisplayName("updateCost - Should throw an OutOfBudgetException if the cost is greater than the project budget")
  void updateCostFailsByCostOutOfBudget() {
    Project project = this.dataMock.getProjects().get(1);
    Task task = this.dataMock.getTasks().get(0);
    BigDecimal cost = new BigDecimal("1500").setScale(2, RoundingMode.FLOOR);

    Exception thrown = catchException(() -> this.projectService.updateCost(project, task, cost));

    assertThat(thrown)
      .isExactlyInstanceOf(OutOfBudgetException.class)
      .hasMessage(
        "Operação inválida! Custo acima do orçamento do projeto.\n" +
        "Orçamento: R$ 1000.00" + "\n" +
        "Custo: R$ 1500.00"
      );

    verify(this.projectRepository, never()).save(any(Project.class));
  }

  @Test
  @DisplayName("updateCost - Should throw an InvalidCostException if the cost value is less than 0")
  void updateCostFailsByNegativeCostValue() {
    Project project = this.dataMock.getProjects().get(1);
    Task task = this.dataMock.getTasks().get(0);
    BigDecimal cost = new BigDecimal("-1");

    Exception thrown = catchException(() -> this.projectService.updateCost(project, task, cost));

    assertThat(thrown)
      .isExactlyInstanceOf(InvalidCostException.class)
      .hasMessage("Custo inválido! Valores negativos não são permitidos. Custo: R$ -1");

    verify(this.projectRepository, never()).save(any(Project.class));
  }

  @Test
  @DisplayName("resetCost - Should successfully set the project cost to BigDecimal zero")
  void resetCostSuccess() {
    Project project = this.dataMock.getProjects().get(1);
    project.setCost(new BigDecimal("1200").setScale(2, RoundingMode.FLOOR));

    ArgumentCaptor<Project> projectCapture = ArgumentCaptor.forClass(Project.class);

    when(this.projectRepository.save(projectCapture.capture())).thenReturn(any(Project.class));

    this.projectService.resetCost(project);

    assertThat(projectCapture.getValue().getCost()).isEqualTo(BigDecimal.ZERO);
    verify(this.projectRepository, times(1)).save(project);
  }
}