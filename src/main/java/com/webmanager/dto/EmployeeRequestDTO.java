package com.webmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmployeeRequestDTO(@NotBlank String name,
                                 @Email @NotBlank String email,
                                 String role) {
}
