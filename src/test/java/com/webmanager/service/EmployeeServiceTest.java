package com.webmanager.service;

import com.webmanager.dto.EmployeeRequestDTO;
import com.webmanager.dto.EmployeeResponseDTO;
import com.webmanager.entity.Employee;
import com.webmanager.exception.EmailAlreadyExistsExecption;
import com.webmanager.exception.EmployeeNotFoundExecption;
import com.webmanager.mapper.EmployeeMapper;
import com.webmanager.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @Mock
    private EmployeeMapper mapper;

    @InjectMocks
    private EmployeeService service;

    @Test
    void shouldCreateEmployee() {

        EmployeeRequestDTO request =
                new EmployeeRequestDTO("João", "joao@email.com", "Developer");

        Employee employee = Employee.builder()
                .name("João")
                .email("joao@email.com")
                .role("Developer")
                .build();

        EmployeeResponseDTO response =
                new EmployeeResponseDTO(UUID.randomUUID(), "João", "joao@email.com", "Developer", null);

        when(repository.existsByEmail("joao@email.com")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(employee);
        when(repository.save(employee)).thenReturn(employee);
        when(mapper.toResponse(employee)).thenReturn(response);

        EmployeeResponseDTO result = service.createEmployee(request);

        assertThat(result.email()).isEqualTo("joao@email.com");

        verify(repository).save(employee);
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {

        EmployeeRequestDTO request =
                new EmployeeRequestDTO("João", "duplicate@email.com", "Developer");

        when(repository.existsByEmail("duplicate@email.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createEmployee(request))
                .isInstanceOf(EmailAlreadyExistsExecption.class);
    }

    @Test
    void shouldFindEmployeeByEmail() {

        String email = "test@email.com";

        Employee employee = Employee.builder()
                .name("Test")
                .email(email)
                .role("Dev")
                .build();

        EmployeeResponseDTO response =
                new EmployeeResponseDTO(UUID.randomUUID(), "Test", email, "Dev", null);

        when(repository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(mapper.toResponse(employee)).thenReturn(response);

        EmployeeResponseDTO result = service.findByEmail(email);

        assertThat(result.email()).isEqualTo(email);
    }

    @Test
    void shouldThrowWhenEmployeeNotFound() {

        when(repository.findByEmail("notfound@email.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByEmail("notfound@email.com"))
                .isInstanceOf(EmployeeNotFoundExecption.class);
    }
}