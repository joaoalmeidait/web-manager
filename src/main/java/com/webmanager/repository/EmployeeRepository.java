package com.webmanager.repository;

import com.webmanager.entity.Employee;
import com.webmanager.enums.EmployeeRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    boolean existsByEmail (String email);

    Optional<Employee> findByEmail(String email);

    boolean existsByCpf(String cpf);

    Page<Employee> findByRole(EmployeeRole role, Pageable pageable);

}
