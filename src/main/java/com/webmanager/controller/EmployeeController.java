package com.webmanager.controller;

import com.webmanager.dto.EmployeeRequestDTO;
import com.webmanager.dto.EmployeeResponseDTO;
import com.webmanager.dto.PageResponseDTO;
import com.webmanager.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<PageResponseDTO<EmployeeResponseDTO>> listAllEmployees(@PageableDefault(size = 10) Pageable pageable){
        var employees =  employeeService.listAllEmployees(pageable);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{email}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeByEmail(@PathVariable String email){
        var employee = employeeService.findByEmail(email);
        return ResponseEntity.ok(employee);
    }
}
