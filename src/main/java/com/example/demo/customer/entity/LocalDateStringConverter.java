package com.example.demo.customer.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Converter
public class LocalDateStringConverter implements AttributeConverter<LocalDate, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String convertToDatabaseColumn(LocalDate localDate) {
        return localDate != null ? localDate.format(FORMATTER) : null;
    }

    @Override
    public LocalDate convertToEntityAttribute(String dateString) {
        return dateString != null && !dateString.trim().isEmpty()
            ? LocalDate.parse(dateString, FORMATTER)
            : null;
    }
}
