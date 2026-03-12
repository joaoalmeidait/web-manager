package com.webmanager.dto.manager;

import jakarta.validation.constraints.Email;

import java.time.LocalDate;

public record ManagerUpdateDTO(String name,
                               @Email String email,
                               String password,
                               String phone,
                               String address,
                               LocalDate birthDate) {
}
