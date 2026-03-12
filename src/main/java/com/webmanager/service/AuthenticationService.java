package com.webmanager.service;

import com.webmanager.dto.login.LoginRequestDTO;
import com.webmanager.dto.login.LoginResponseDTO;
import com.webmanager.entity.Manager;
import com.webmanager.exception.InvalidCredentialsException;
import com.webmanager.repository.ManagerRepository;
import com.webmanager.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final ManagerRepository managerRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(LoginRequestDTO requestDTO) {
        Manager manager = managerRepository.findByEmail(requestDTO.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciais Inválidas"));

        if (!passwordEncoder.matches(requestDTO.password(), manager.getPassword())) {
            throw new InvalidCredentialsException("Credenciais Inválidas");
        }

        String token = jwtService.generateToken(requestDTO.email());

        return new LoginResponseDTO(token);
    }
}
