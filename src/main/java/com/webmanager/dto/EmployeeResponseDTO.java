package com.webmanager.dto;

import com.webmanager.entity.Manager;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeeResponseDTO(UUID id,
                                  String name,
                                  String email,
                                  String role,
                                  String managerName,
                                  LocalDateTime createdAt) {
}
