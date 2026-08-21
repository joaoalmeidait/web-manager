package com.webmanager.service;

import com.webmanager.BaseTest;
import com.webmanager.dto.login.LoginRequestDTO;
import com.webmanager.dto.login.LoginResponseDTO;
import com.webmanager.entity.Manager;
import com.webmanager.enums.Role;
import com.webmanager.exception.InvalidCredentialsException;
import com.webmanager.repository.ManagerRepository;
import com.webmanager.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest extends BaseTest {

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldLoginSuccessfully() {
        LoginRequestDTO request = new LoginRequestDTO("test@email.com", "password");

        Manager manager = Manager.builder()
                .email("test@email.com")
                .password("encodedPassword")
                .role(Role.MANAGER)
                .build();

        when(managerRepository.findByEmail("test@email.com")).thenReturn(Optional.of(manager));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken("test@email.com")).thenReturn("jwt-token");

        LoginResponseDTO response = authenticationService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void shouldThrowWhenEmailNotFound() {
        LoginRequestDTO request = new LoginRequestDTO("notfound@email.com", "password");

        when(managerRepository.findByEmail("notfound@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciais Inválidas");
    }

    @Test
    void shouldThrowWhenPasswordDoesNotMatch() {
        LoginRequestDTO request = new LoginRequestDTO("test@email.com", "wrongpassword");

        Manager manager = Manager.builder()
                .email("test@email.com")
                .password("encodedPassword")
                .role(Role.MANAGER)
                .build();

        when(managerRepository.findByEmail("test@email.com")).thenReturn(Optional.of(manager));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciais Inválidas");
    }
}
