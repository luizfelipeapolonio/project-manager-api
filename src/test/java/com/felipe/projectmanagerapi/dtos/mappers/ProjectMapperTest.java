package com.felipe.projectmanagerapi.dtos.mappers;

import com.felipe.projectmanagerapi.dtos.ProjectResponseDTO;
import com.felipe.projectmanagerapi.enums.PriorityLevel;
import com.felipe.projectmanagerapi.models.Project;
import com.felipe.projectmanagerapi.models.User;
import com.felipe.projectmanagerapi.models.Workspace;
import com.felipe.projectmanagerapi.utils.ConvertDateFormat;
import com.felipe.projectmanagerapi.utils.GenerateMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

public class ProjectMapperTest {

  @Spy
  ProjectMapper projectMapper;

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
  @DisplayName("toDTO - Should successfully convert a Project entity instance to a ProjectResponseDTO")
  void convertProjectEntityToDTOSuccess() {
    Project project = this.dataMock.getProjects().get(1);
    User projectOwner = project.getOwner();
    Workspace projectWorkspace = project.getWorkspace();

    ProjectResponseDTO convertedProjectDTO = this.projectMapper.toDTO(project);

    assertThat(convertedProjectDTO.id()).isEqualTo(project.getId());
    assertThat(convertedProjectDTO.name()).isEqualTo(project.getName());
    assertThat(convertedProjectDTO.priority()).isEqualTo(project.getPriority().getValue());
    assertThat(convertedProjectDTO.category()).isEqualTo(project.getCategory());
    assertThat(convertedProjectDTO.description()).isEqualTo(project.getDescription());
    assertThat(convertedProjectDTO.budget()).isEqualTo(project.getBudget().toString());
    assertThat(convertedProjectDTO.deadline())
      .isEqualTo(ConvertDateFormat.convertDateToFormattedString(project.getDeadline()));
    assertThat(convertedProjectDTO.createdAt()).isEqualTo(project.getCreatedAt());
    assertThat(convertedProjectDTO.updatedAt()).isEqualTo(project.getUpdatedAt());
    assertThat(convertedProjectDTO.ownerId()).isEqualTo(projectOwner.getId());
    assertThat(convertedProjectDTO.workspaceId()).isEqualTo(projectWorkspace.getId());
  }

  @Test
  @DisplayName("convertValueToPriorityLevel - Should successfully convert the string value to its corresponding PriorityLevel value")
  void convertValueToPriorityLevelSuccess() {
    PriorityLevel highPriority = this.projectMapper.convertValueToPriorityLevel("alta");
    PriorityLevel mediumPriority = this.projectMapper.convertValueToPriorityLevel("media");
    PriorityLevel lowPriority = this.projectMapper.convertValueToPriorityLevel("baixa");

    assertThat(highPriority).isEqualTo(PriorityLevel.HIGH);
    assertThat(mediumPriority).isEqualTo(PriorityLevel.MEDIUM);
    assertThat(lowPriority).isEqualTo(PriorityLevel.LOW);
  }

  @Test
  @DisplayName("convertValueToPriorityLevel - Should throw an IllegalArgumentException if the given value is invalid")
  void convertValueToPriorityLevelFailsByInvalidValue() {
    Exception thrown = catchException(() -> this.projectMapper.convertValueToPriorityLevel("randomValue"));

    assertThat(thrown)
      .isExactlyInstanceOf(IllegalArgumentException.class)
      .hasMessage("Não foi possível converter o valor: 'randomValue' para PriorityLevel");
  }
}
