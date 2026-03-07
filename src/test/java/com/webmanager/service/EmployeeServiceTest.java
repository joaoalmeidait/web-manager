package com.webmanager.service;

import com.webmanager.dto.EmployeeRequestDTO;
import com.webmanager.dto.EmployeeResponseDTO;
import com.webmanager.dto.PageResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
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

    @Test
    void shouldReturnPagedEmployees() {

        Pageable pageable = PageRequest.of(0, 10);

        Employee employee1 = Employee.builder()
                .name("João")
                .email("joao@email.com")
                .role("Developer")
                .build();

        Employee employee2 = Employee.builder()
                .name("Maria")
                .email("maria@email.com")
                .role("Manager")
                .build();

        Page<Employee> page = new PageImpl<>(List.of(employee1, employee2));

        EmployeeResponseDTO response1 =
                new EmployeeResponseDTO(UUID.randomUUID(), "João", "joao@email.com", "Developer", null);

        EmployeeResponseDTO response2 =
                new EmployeeResponseDTO(UUID.randomUUID(), "Maria", "maria@email.com", "Manager", null);

        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toResponse(employee1)).thenReturn(response1);
        when(mapper.toResponse(employee2)).thenReturn(response2);

        PageResponseDTO<EmployeeResponseDTO> result =
                service.listAllEmployees(pageable);

        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).email()).isEqualTo("joao@email.com");
        assertThat(result.content().get(1).email()).isEqualTo("maria@email.com");

        verify(repository).findAll(pageable);
    }
}