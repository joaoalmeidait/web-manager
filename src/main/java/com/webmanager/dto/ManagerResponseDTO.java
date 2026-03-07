package com.webmanager.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ManagerResponseDTO (UUID id,
                                  String name,
                                  String email,
                                  String phone,
                                  String cpf,
                                  LocalDate birthDate,
                                  String address,
                                  LocalDateTime createdAt,
                                  LocalDateTime updatedAt) {
}
