package com.webmanager.utils;

import com.webmanager.exception.CPFAlreadyExistsException;
import com.webmanager.exception.EmailAlreadyExistsExecption;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ValidationUtils {

    public void validateUniqueEmail(String email, Supplier<Boolean> existsCheck) {
        if (existsCheck.get()) {
            throw new EmailAlreadyExistsExecption("Email já cadastrado.");
        }
    }

    public void validateCPF(String cpf, Supplier<Boolean> existsCheck) {
        if (existsCheck.get()) {
            throw new CPFAlreadyExistsException("CPF já cadastrado");
        }
    }

}
