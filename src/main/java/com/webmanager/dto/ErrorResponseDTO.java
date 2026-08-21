package com.webmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(int status,
                               String message,
                               Map<String, String> errors) {

    public static ErrorResponseDTO of(int status, String message) {
        return new ErrorResponseDTO(status, message, null);
    }
}
