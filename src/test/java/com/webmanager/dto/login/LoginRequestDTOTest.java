package com.webmanager.dto.login;

import com.webmanager.BaseTest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestDTOTest extends BaseTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldPassWithValidData() {
        LoginRequestDTO dto = new LoginRequestDTO("test@email.com", "password");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWithBlankEmail() {
        LoginRequestDTO dto = new LoginRequestDTO("", "password");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    void shouldFailWithNullEmail() {
        LoginRequestDTO dto = new LoginRequestDTO(null, "password");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldFailWithInvalidEmailFormat() {
        LoginRequestDTO dto = new LoginRequestDTO("not-an-email", "password");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).contains("Email inválido");
    }

    @Test
    void shouldFailWithBlankPassword() {
        LoginRequestDTO dto = new LoginRequestDTO("test@email.com", "");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("password");
    }

    @Test
    void shouldFailWithNullPassword() {
        LoginRequestDTO dto = new LoginRequestDTO("test@email.com", null);

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }
}
