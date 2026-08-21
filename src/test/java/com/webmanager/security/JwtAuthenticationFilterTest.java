package com.webmanager.security;

import com.webmanager.BaseTest;
import com.webmanager.entity.Manager;
import com.webmanager.enums.Role;
import com.webmanager.repository.ManagerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@Transactional
class JwtAuthenticationFilterTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Manager manager;

    @BeforeEach
    void setUp() {
        manager = Manager.builder()
                .name("Filter Test Manager")
                .email("filter@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.MANAGER)
                .phone("11999999999")
                .cpf("52998224725")
                .build();

        managerRepository.save(manager);
    }

    @Test
    void shouldRejectRequestWithoutAuthHeader() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRequestWithBasicPrefix() throws Exception {
        String token = jwtService.generateToken("filter@test.com");

        mockMvc.perform(get("/employees")
                        .header("Authorization", "Basic " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowRequestWithValidToken() throws Exception {
        String token = jwtService.generateToken("filter@test.com");

        mockMvc.perform(get("/employees")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectExpiredToken() throws Exception {
        String expiredToken = jwtService.generateExpiredToken("filter@test.com");

        mockMvc.perform(get("/employees")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectTokenForNonExistentUser() throws Exception {
        String token = jwtService.generateToken("nonexistent@email.com");

        mockMvc.perform(get("/employees")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectTamperedToken() throws Exception {
        String token = jwtService.generateToken("filter@test.com");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        mockMvc.perform(get("/employees")
                        .header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldSetRoleAuthorityForManager() throws Exception {
        String token = jwtService.generateToken("filter@test.com");

        mockMvc.perform(get("/employees")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldSetRoleAuthorityForAdmin() throws Exception {
        Manager admin = Manager.builder()
                .name("Admin Filter Test")
                .email("adminfilter@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.ADMIN)
                .phone("11988888888")
                .cpf("11144477735")
                .build();
        managerRepository.save(admin);

        String adminToken = jwtService.generateToken("adminfilter@test.com");

        mockMvc.perform(get("/employees")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyManagerFromCreatingManager() throws Exception {
        String managerToken = jwtService.generateToken("filter@test.com");

        mockMvc.perform(get("/managers")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
    }
}
