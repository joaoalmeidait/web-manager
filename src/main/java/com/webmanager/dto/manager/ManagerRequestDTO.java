package com.webmanager.dto.manager;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record ManagerRequestDTO(@NotBlank String name,
                                @Email @NotBlank String email,
                                @NotBlank String phone,
                                @NotBlank @CPF String cpf,
                                String address,
                                LocalDate birthDate) {
}
