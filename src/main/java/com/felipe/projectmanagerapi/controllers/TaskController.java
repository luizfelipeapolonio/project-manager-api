package com.felipe.projectmanagerapi.controllers;

import com.felipe.projectmanagerapi.dtos.TaskCreateDTO;
import com.felipe.projectmanagerapi.dtos.TaskResponseDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
