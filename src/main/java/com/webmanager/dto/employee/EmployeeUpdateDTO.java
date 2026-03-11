package com.webmanager.dto.employee;

import jakarta.validation.constraints.Email;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeUpdateDTO(String name,
                                @Email String email,
                                String phone,
                                String role,
                                UUID managerId,
                                String address,
                                LocalDate birthDate) {
}
