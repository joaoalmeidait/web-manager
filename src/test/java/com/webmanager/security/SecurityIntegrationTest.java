package com.webmanager.security;

import com.webmanager.BaseTest;
import com.webmanager.entity.Manager;
import com.webmanager.enums.Role;
import com.webmanager.repository.ManagerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        Manager manager = Manager.builder()
                .name("Admin Test")
                .email("admin@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.ADMIN)
                .phone("11999999999")
                .cpf("52998224725")
                .build();

        managerRepository.save(manager);
    }

    @Test
    void shouldReturn401WhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Não autenticado"));
    }

    @Test
    void shouldReturn401WhenInvalidTokenProvided() throws Exception {
        mockMvc.perform(get("/employees")
                        .header("Authorization", "Bearer invalidtoken123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenAccessDenied() throws Exception {
        Manager regularManager = Manager.builder()
                .name("Regular Manager")
                .email("manager@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.MANAGER)
                .phone("11988888888")
                .cpf("11144477735")
                .build();
        managerRepository.save(regularManager);

        String managerToken = jwtService.generateToken("manager@test.com");

        mockMvc.perform(post("/managers")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "New Manager",
                                  "email": "new@test.com",
                                  "phone": "11977777777",
                                  "cpf": "52998224725",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Acesso negado"));
    }

    @Test
    void shouldAllowAdminToCreateManager() throws Exception {
        String adminToken = jwtService.generateToken("admin@test.com");

        mockMvc.perform(post("/managers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "New Manager",
                                  "email": "newmanager@test.com",
                                  "phone": "11977777777",
                                  "cpf": "11144477735",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn401WhenTokenIsExpired() throws Exception {
        String expiredToken = jwtService.generateExpiredToken("admin@test.com");

        mockMvc.perform(get("/employees")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400WhenLoginWithBlankEmail() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void shouldReturn400WhenLoginWithBlankPassword() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "admin@test.com",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void shouldReturn400WhenLoginWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void shouldReturn400WhenLoginWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "admin@test.com",
                                  "password": "wrongpassword"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Credenciais Inválidas"));
    }

    @Test
    void shouldSetXContentTypeOptionsHeader() throws Exception {
        String adminToken = jwtService.generateToken("admin@test.com");

        mockMvc.perform(get("/employees")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void shouldSetXFrameOptionsHeader() throws Exception {
        String adminToken = jwtService.generateToken("admin@test.com");

        mockMvc.perform(get("/employees")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }
}
