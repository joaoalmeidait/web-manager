package com.webmanager.repository;

import com.webmanager.entity.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository repository;

    @Test
    void shouldFindEmployeeByEmail() {

        Employee employee = Employee.builder()
                .name("João")
                .email("joao@email.com")
                .role("Developer")
                .build();

        repository.save(employee);

        Optional<Employee> result = repository.findByEmail("joao@email.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("João");
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {

        Employee employee = new Employee();
        employee.setName("Maria");
        employee.setEmail("maria@email.com");
        employee.setRole("Manager");

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
                .role("Dev")
                .build();

        Employee employee2 = Employee.builder()
                .name("Maria")
                .email("duplicate@email.com")
                .role("Manager")
                .build();

        repository.save(employee1);

        assertThatThrownBy(() -> repository.saveAndFlush(employee2))
                .isInstanceOf(Exception.class);
    }
}