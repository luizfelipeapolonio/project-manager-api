package com.felipe.projectmanagerapi.dtos;

public record TaskCreateDTO(String name, String description, String cost, String projectId) {
}
