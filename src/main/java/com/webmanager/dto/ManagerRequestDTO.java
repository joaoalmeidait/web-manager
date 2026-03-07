package com.webmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ManagerRequestDTO(@NotBlank String name,
                                @Email @NotBlank String email) {
}
