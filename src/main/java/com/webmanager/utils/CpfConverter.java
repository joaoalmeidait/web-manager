package com.webmanager.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CpfConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String cpf) {
        if (cpf == null) return null;
        return cpf.replaceAll("\\D", "");
    }

    @Override
    public String convertToEntityAttribute(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;

        return cpf.replaceFirst(
                "(\\d{3})(\\d{3})(\\d{3})(\\d{2})",
                "$1.$2.$3-$4"
        );
    }
}