package com.webmanager.controller;

import com.webmanager.dto.PageResponseDTO;
import com.webmanager.dto.employee.EmployeeRequestDTO;
import com.webmanager.dto.employee.EmployeeResponseDTO;
import com.webmanager.dto.employee.EmployeeUpdateDTO;
import com.webmanager.enums.EmployeeRole;
import com.webmanager.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO dto){
        var employee = employeeService.createEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@Valid @PathVariable UUID id, @RequestBody EmployeeUpdateDTO dto) {
        var employee = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(employee);
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<EmployeeResponseDTO>> listAllEmployees(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) EmployeeRole role) {
        var employees = employeeService.listAllEmployees(pageable, role);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable UUID id){
        var employee = employeeService.findById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeByEmail(@PathVariable String email){
        var employee = employeeService.findByEmail(email);
        return ResponseEntity.ok(employee);
    }
}
