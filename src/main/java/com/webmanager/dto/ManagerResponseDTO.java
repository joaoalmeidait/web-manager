package com.webmanager.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ManagerResponseDTO (UUID id,
                                  String name,
                                  String email,
                                  LocalDateTime createdAt){
}
