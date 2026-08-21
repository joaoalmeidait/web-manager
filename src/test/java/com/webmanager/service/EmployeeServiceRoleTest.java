package com.webmanager.service;

import com.webmanager.dto.PageResponseDTO;
import com.webmanager.dto.employee.EmployeeRequestDTO;
import com.webmanager.dto.employee.EmployeeResponseDTO;
import com.webmanager.dto.employee.EmployeeUpdateDTO;
import com.webmanager.entity.Employee;
import com.webmanager.entity.Manager;
import com.webmanager.enums.EmployeeRole;
import com.webmanager.mapper.EmployeeMapper;
import com.webmanager.mapper.EmployeeMapperImpl;
import com.webmanager.repository.EmployeeRepository;
import com.webmanager.repository.ManagerRepository;
import com.webmanager.utils.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceRoleTest {

    @Mock
    private EmployeeRepository repository;

    @Mock
    private ManagerRepository managerRepository;

    private final EmployeeMapper mapper = new EmployeeMapperImpl();

    private final ValidationUtils validationUtils = new ValidationUtils();

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService(repository, mapper, managerRepository, validationUtils);
    }

    @Test
    void shouldCreateEmployeeWithRole() {

        EmployeeRequestDTO request = new EmployeeRequestDTO(
                "João", "joao@email.com", "11999999999", EmployeeRole.DEVELOPER, null,
                "12345678909", "Rua A", LocalDate.of(1990, 1, 1));

        Manager manager = Manager.builder().id(UUID.randomUUID()).build();

        when(repository.existsByCpf(anyString())).thenReturn(false);
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(managerRepository.findByName("Sistema")).thenReturn(Optional.of(manager));
        when(repository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponseDTO result = service.createEmployee(request);

        assertThat(result.role()).isEqualTo(EmployeeRole.DEVELOPER);

        verify(repository).save(argThat(e ->
                e.getRole() == EmployeeRole.DEVELOPER && e.getManager() == manager));
    }

    @Test
    void shouldUpdateEmployeeRole() {

        UUID id = UUID.randomUUID();

        Employee employee = Employee.builder()
                .id(id)
                .name("João")
                .email("joao@email.com")
                .role(EmployeeRole.DEVELOPER)
                .build();

        EmployeeUpdateDTO dto = new EmployeeUpdateDTO(null, null, null, EmployeeRole.TECH_LEAD, null, null, null);

        when(repository.findById(id)).thenReturn(Optional.of(employee));
        when(repository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponseDTO result = service.updateEmployee(id, dto);

        assertThat(employee.getRole()).isEqualTo(EmployeeRole.TECH_LEAD);
        assertThat(result.role()).isEqualTo(EmployeeRole.TECH_LEAD);
    }

    @Test
    void shouldListEmployeesFilteredByRole() {

        Pageable pageable = PageRequest.of(0, 10);

        Employee employee = Employee.builder()
                .id(UUID.randomUUID())
                .name("João")
                .email("joao@email.com")
                .role(EmployeeRole.DEVELOPER)
                .build();

        Page<Employee> page = new PageImpl<>(List.of(employee));

        when(repository.findByRole(EmployeeRole.DEVELOPER, pageable)).thenReturn(page);

        PageResponseDTO<EmployeeResponseDTO> result = service.listAllEmployees(pageable, EmployeeRole.DEVELOPER);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).role()).isEqualTo(EmployeeRole.DEVELOPER);

        verify(repository).findByRole(EmployeeRole.DEVELOPER, pageable);
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void shouldListAllEmployeesWhenRoleNotInformed() {

        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        PageResponseDTO<EmployeeResponseDTO> result = service.listAllEmployees(pageable, null);

        assertThat(result.content()).isEmpty();

        verify(repository).findAll(pageable);
        verify(repository, never()).findByRole(any(), any());
    }
}