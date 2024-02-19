package com.felipe.projectmanagerapi.enums.converters;

import com.felipe.projectmanagerapi.enums.PriorityLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.assertThat;

public class PriorityLevelConverterTest {

  @Spy
  PriorityLevelConverter priorityLevelConverter;

  private AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    this.closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void tearDown() throws Exception {
    this.closeable.close();
  }

  @Test
  @DisplayName("convertToDatabaseColumn - Should successfully convert the priority level to its corresponding integer")
  void convertPriorityLevelToDatabaseIntegerColumnSuccess() {
    Integer highPriority = this.priorityLevelConverter.convertToDatabaseColumn(PriorityLevel.HIGH);
    Integer mediumPriority = this.priorityLevelConverter.convertToDatabaseColumn(PriorityLevel.MEDIUM);
    Integer lowPriority = this.priorityLevelConverter.convertToDatabaseColumn(PriorityLevel.LOW);

    assertThat(highPriority).isEqualTo(1);
    assertThat(mediumPriority).isEqualTo(2);
    assertThat(lowPriority).isEqualTo(3);
  }

  @Test
  @DisplayName("convertToDatabaseColumn - Should return the 3 integer value if priority level is null")
  void convertPriorityLevelToDatabaseIntegerColumnIfPriorityIsNull() {
    Integer value = this.priorityLevelConverter.convertToDatabaseColumn(null);
    assertThat(value).isEqualTo(3);
  }

  @Test
  @DisplayName("convertToEntityAttribute - Should successfully convert the returned integer value to its corresponding enum constant")
  void convertIntegerValueToPriorityLevelEnumSuccess() {
    PriorityLevel highPriority = this.priorityLevelConverter.convertToEntityAttribute(1);
    PriorityLevel mediumPriority = this.priorityLevelConverter.convertToEntityAttribute(2);
    PriorityLevel lowPriority = this.priorityLevelConverter.convertToEntityAttribute(3);

    assertThat(highPriority).isEqualTo(PriorityLevel.HIGH);
    assertThat(mediumPriority).isEqualTo(PriorityLevel.MEDIUM);
    assertThat(lowPriority).isEqualTo(PriorityLevel.LOW);
  }
}
