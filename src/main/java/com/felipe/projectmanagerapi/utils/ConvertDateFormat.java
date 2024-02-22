package com.felipe.projectmanagerapi.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ConvertDateFormat {
  private static final String OUTPUT_DATE_FORMAT = "dd-MM-yyyy";

  public static LocalDate convertStringToDateFormat(String date) {
    if(date == null || date.isEmpty()) return null;
    return LocalDate.parse(date, DateTimeFormatter.ofPattern(OUTPUT_DATE_FORMAT));
  }

  public static String convertDateFromDatabaseToRightFormat(LocalDate date) {
    if(date == null) return null;
    // The 'date' parameter comes from the database in the format "yyyy-MM-dd" Ex: 2024-01-01
    DateTimeFormatter databaseDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter correctOutputFormat = DateTimeFormatter.ofPattern(OUTPUT_DATE_FORMAT);
    LocalDate databaseDate = LocalDate.parse(date.toString(), databaseDateFormat);
    return correctOutputFormat.format(databaseDate);
  }
}
