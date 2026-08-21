package com.webmanager.dto.employee;

import com.webmanager.enums.EmployeeRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeRequestDTO(@NotBlank String name,
                                 @Email @NotBlank String email,
                                 @NotBlank String phone,
                                 @NotNull EmployeeRole role,
                                 UUID managerId,
                                 @NotBlank @CPF String cpf,
                                 String address,
                                 LocalDate birthDate) {
}
