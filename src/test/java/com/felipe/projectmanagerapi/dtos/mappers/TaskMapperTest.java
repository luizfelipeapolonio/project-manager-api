package com.felipe.projectmanagerapi.dtos.mappers;

import com.felipe.projectmanagerapi.dtos.TaskResponseDTO;
import com.felipe.projectmanagerapi.models.Task;
import com.felipe.projectmanagerapi.utils.GenerateMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskMapperTest {

  @Spy
  TaskMapper taskMapper;

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
  @DisplayName("toDTO - Should successfully convert a Task entity to a TaskResponseDTO")
  void convertTaskEntityToTaskResponseDTOSuccess() {
    Task task = this.dataMock.getTasks().get(0);

    TaskResponseDTO convertedTaskDTO = this.taskMapper.toDTO(task);

    assertThat(convertedTaskDTO.id()).isEqualTo(task.getId());
    assertThat(convertedTaskDTO.name()).isEqualTo(task.getName());
    assertThat(convertedTaskDTO.description()).isEqualTo(task.getDescription());
    assertThat(convertedTaskDTO.cost()).isEqualTo(task.getCost().toString());
    assertThat(convertedTaskDTO.createdAt()).isEqualTo(task.getCreatedAt());
    assertThat(convertedTaskDTO.updatedAt()).isEqualTo(task.getUpdatedAt());
    assertThat(convertedTaskDTO.projectId()).isEqualTo(task.getProject().getId());
    assertThat(convertedTaskDTO.ownerId()).isEqualTo(task.getOwner().getId());
  }
}
