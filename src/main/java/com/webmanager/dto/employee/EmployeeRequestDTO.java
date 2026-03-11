package com.webmanager.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeRequestDTO(@NotBlank String name,
                                 @Email @NotBlank String email,
                                 @NotBlank String phone,
                                 String role,
                                 UUID managerId,
                                 @NotBlank @CPF String cpf,
                                 String address,
                                 LocalDate birthDate) {
}
