package com.felipe.projectmanagerapi.enums.converters;

import com.felipe.projectmanagerapi.enums.PriorityLevel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter
public class PriorityLevelConverter implements AttributeConverter<PriorityLevel, Integer> {
  @Override
  public Integer convertToDatabaseColumn(PriorityLevel priority) {
    if(priority == null) {
      return PriorityLevel.LOW.getLevel();
    }
    return priority.getLevel();
  }

  @Override
  public PriorityLevel convertToEntityAttribute(Integer level) {
    return Stream.of(PriorityLevel.values())
      .filter(enumConstant -> enumConstant.getLevel() == level)
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("Valor do Enum de prioridade inválido: " + level));
  }
}
