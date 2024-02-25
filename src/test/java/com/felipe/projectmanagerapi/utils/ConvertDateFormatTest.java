package com.felipe.projectmanagerapi.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

public class ConvertDateFormatTest {

  @Test
  @DisplayName("convertStringToDateFormat - Should successfully convert a string date of the 'dd-MM-yyyy' format into a LocalDate instance")
  void convertStringToDateFormatSuccess() {
    String stringDate = "01-01-2025";
    DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    LocalDate convertedDate = ConvertDateFormat.convertFormattedStringToDate(stringDate);

    assertThat(convertedDate).isEqualTo(LocalDate.parse("01-01-2025", outputFormat));
    assertThat(convertedDate.format(outputFormat)).isEqualTo("01-01-2025");
  }

  @Test
  @DisplayName("convertDateFromDatabaseToRightFormat - Should successfully convert a date coming from database into the right format")
  void convertDateFromDatabaseToRightFormatSuccess() {
    DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate inputDate = LocalDate.parse("2025-01-01", inputFormat);

    String formattedDate = ConvertDateFormat.convertDateToFormattedString(inputDate);

    assertThat(formattedDate).isEqualTo("01-01-2025");
  }
}
