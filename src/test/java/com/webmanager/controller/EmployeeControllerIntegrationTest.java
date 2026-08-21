package com.webmanager.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webmanager.BaseTest;
import com.webmanager.entity.Manager;
import com.webmanager.enums.Role;
import com.webmanager.repository.ManagerRepository;
import com.webmanager.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmployeeControllerIntegrationTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String token;

    @BeforeEach
    void setUp() {
        Manager manager = Manager.builder()
                .name("Sistema")
                .email("sistema@webmanager.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.MANAGER)
                .phone("11999999999")
                .cpf("12345678909")
                .build();

        managerRepository.save(manager);
        token = jwtService.generateToken(manager.getEmail());
    }

    @Test
    void shouldReturnClearMessageWhenFilteringByInvalidRole() throws Exception {
        mockMvc.perform(get("/employees")
                        .header("Authorization", "Bearer " + token)
                        .param("role", "INVALID_ROLE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Valores aceitos")));
    }

    @Test
    void shouldReturnClearMessageWhenCreatingWithInvalidRole() throws Exception {
        mockMvc.perform(post("/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "João",
                                  "email": "joao@email.com",
                                  "phone": "11999999999",
                                  "role": "INVALID_ROLE",
                                  "cpf": "12345678901"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Valores aceitos")));
    }

    @Test
    void shouldRequireRoleOnCreate() throws Exception {
        mockMvc.perform(post("/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "João",
                                  "email": "joao@email.com",
                                  "phone": "11999999999",
                                  "cpf": "52998224725"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.role").exists());
    }

    @Test
    void shouldCreateAndFilterEmployeeByRole() throws Exception {
        createEmployee("joao@email.com", "DEVELOPER", "52998224725");
        createEmployee("maria@email.com", "QA_ENGINEER", "11144477735");

        mockMvc.perform(get("/employees")
                        .header("Authorization", "Bearer " + token)
                        .param("role", "DEVELOPER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].role").value("DEVELOPER"));
    }

    @Test
    void shouldUpdateEmployeeRole() throws Exception {
        String id = createEmployee("joao@email.com", "DEVELOPER", "52998224725");

        mockMvc.perform(put("/employees/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"TECH_LEAD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("TECH_LEAD"));
    }

    private String createEmployee(String email, String role, String cpf) throws Exception {
        MvcResult result = mockMvc.perform(post("/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "João",
                                  "email": "%s",
                                  "phone": "11999999999",
                                  "role": "%s",
                                  "cpf": "%s"
                                }
                                """.formatted(email, role, cpf)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asText();
    }
}