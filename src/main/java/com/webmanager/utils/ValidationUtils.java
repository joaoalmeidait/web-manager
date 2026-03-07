package com.webmanager.utils;

import com.webmanager.exception.EmailAlreadyExistsExecption;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ValidationUtils {

    public void validateUniqueEmail(String email, Supplier<Boolean> existesCheck) {
        if (existesCheck.get()) {
            throw new EmailAlreadyExistsExecption("Email já cadastrado.");
        }
    }

}
