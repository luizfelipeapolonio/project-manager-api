package com.felipe.projectmanagerapi.controllers;

import com.felipe.projectmanagerapi.dtos.TaskCreateDTO;
import com.felipe.projectmanagerapi.dtos.TaskResponseDTO;
import com.felipe.projectmanagerapi.dtos.TaskUpdateDTO;
import com.felipe.projectmanagerapi.dtos.mappers.TaskMapper;
import com.felipe.projectmanagerapi.enums.ResponseConditionStatus;
import com.felipe.projectmanagerapi.models.Task;
import com.felipe.projectmanagerapi.services.TaskService;
import com.felipe.projectmanagerapi.utils.CustomResponseBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskService taskService;
  private final TaskMapper taskMapper;

  public TaskController(TaskService taskService, TaskMapper taskMapper) {
    this.taskService = taskService;
    this.taskMapper = taskMapper;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CustomResponseBody<TaskResponseDTO> create(@RequestBody @Valid TaskCreateDTO task) {
    Task createdTask = this.taskService.create(task);
    TaskResponseDTO taskResponseDTO = this.taskMapper.toDTO(createdTask);

    CustomResponseBody<TaskResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.CREATED);
    response.setMessage("Task criada com sucesso");
    response.setData(taskResponseDTO);
    return response;
  }

  @GetMapping("/{taskId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<TaskResponseDTO> getById(@PathVariable @NotNull @NotBlank String taskId) {
    Task task = this.taskService.getById(taskId);
    TaskResponseDTO taskResponseDTO = this.taskMapper.toDTO(task);

    CustomResponseBody<TaskResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Task encontrada");
    response.setData(taskResponseDTO);
    return response;
  }

  @DeleteMapping("/{taskId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Map<String, TaskResponseDTO>> delete(@PathVariable @NotNull @NotBlank String taskId) {
    Task deletedTask = this.taskService.delete(taskId);
    TaskResponseDTO taskResponseDTO = this.taskMapper.toDTO(deletedTask);

    Map<String, TaskResponseDTO> taskResponseMap = new HashMap<>(1);
    taskResponseMap.put("deletedTask", taskResponseDTO);

    CustomResponseBody<Map<String, TaskResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Task excluída com sucesso");
    response.setData(taskResponseMap);
    return response;
  }

  @PatchMapping("/{taskId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<TaskResponseDTO> update(
    @PathVariable @NotNull @NotBlank String taskId,
    @RequestBody @NotNull @Valid TaskUpdateDTO task
  ) {
    Task updatedTask = this.taskService.update(taskId, task);
    TaskResponseDTO taskResponseDTO = this.taskMapper.toDTO(updatedTask);

    CustomResponseBody<TaskResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Task atualizada com sucesso");
    response.setData(taskResponseDTO);
    return response;
  }

  @GetMapping("/projects/{projectId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<List<TaskResponseDTO>> getAllFromProject(@PathVariable @NotNull @NotBlank String projectId) {
    List<Task> allTasks = this.taskService.getAllFromProject(projectId);
    List<TaskResponseDTO> allTasksDTO = allTasks.stream().map(this.taskMapper::toDTO).toList();

    CustomResponseBody<List<TaskResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todas as tasks do projeto de ID: '" + projectId + "'");
    response.setData(allTasksDTO);
    return response;
  }
}
