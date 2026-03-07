package com.webmanager.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeeResponseDTO(UUID id,
                                  String name,
                                  String email,
                                  String role,
                                  LocalDateTime createdAt) {
}
