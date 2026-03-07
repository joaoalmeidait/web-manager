package com.webmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record EmployeeRequestDTO(@NotBlank String name,
                                 @Email @NotBlank String email,
                                 String role,
                                 UUID managerId) {
}
