package com.webmanager.dto.employee;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeeResponseDTO(UUID id,
                                  String name,
                                  String email,
                                  String phone,
                                  String cpf,
                                  String role,
                                  String address,
                                  LocalDate birthDate,
                                  String managerName,
                                  LocalDateTime createdAt,
                                  LocalDateTime updatedAt
) {
}
