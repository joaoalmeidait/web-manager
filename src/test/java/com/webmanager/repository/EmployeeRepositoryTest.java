package com.webmanager.repository;

import com.webmanager.BaseTest;
import com.webmanager.entity.Employee;
import com.webmanager.enums.EmployeeRole;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
class EmployeeRepositoryTest extends BaseTest {

    @Autowired
    private EmployeeRepository repository;

    @Test
    void shouldFindEmployeeByEmail() {

        Employee employee = Employee.builder()
                .name("João")
                .email("joao@email.com")
                .phone("11999999999")
                .cpf("12345678901")
                .role(EmployeeRole.DEVELOPER)
                .build();

        repository.save(employee);

        Optional<Employee> result = repository.findByEmail("joao@email.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("João");
    }

    @Test
    void shouldFindEmployeeById() {

        Employee employee = Employee.builder()
                .name("Maria")
                .email("maria@email.com")
                .phone("11999999999")
                .cpf("12345678905")
                .role(EmployeeRole.QA_ENGINEER)
                .build();

        Employee saved = repository.save(employee);

        Optional<Employee> result = repository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {

        Employee employee = new Employee();
        employee.setName("Maria");
        employee.setEmail("maria@email.com");
        employee.setPhone("11999999999");
        employee.setCpf("12345678902");
        employee.setRole(EmployeeRole.DEVELOPER);

        repository.save(employee);

        boolean exists = repository.existsByEmail("maria@email.com");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {

        Optional<Employee> result = repository.findByEmail("notfound@email.com");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotAllowDuplicateEmails() {

        Employee employee1 = Employee.builder()
                .name("João")
                .email("duplicate@email.com")
                .phone("11999999999")
                .cpf("12345678903")
                .role(EmployeeRole.QA_ENGINEER)
                .build();

        Employee employee2 = Employee.builder()
                .name("Maria")
                .email("duplicate@email.com")
                .phone("11999999999")
                .cpf("12345678904")
                .role(EmployeeRole.DEVELOPER)
                .build();

        repository.save(employee1);

        assertThatThrownBy(() -> repository.saveAndFlush(employee2))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldFindEmployeesByRole() {

        Employee developer = Employee.builder()
                .name("João")
                .email("joao@email.com")
                .phone("11999999999")
                .cpf("12345678901")
                .role(EmployeeRole.DEVELOPER)
                .build();

        Employee qa = Employee.builder()
                .name("Maria")
                .email("maria@email.com")
                .phone("11999999999")
                .cpf("12345678905")
                .role(EmployeeRole.QA_ENGINEER)
                .build();

        repository.save(developer);
        repository.save(qa);

        Page<Employee> result = repository.findByRole(EmployeeRole.DEVELOPER, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRole()).isEqualTo(EmployeeRole.DEVELOPER);
    }
}