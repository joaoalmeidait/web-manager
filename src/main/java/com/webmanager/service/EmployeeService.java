package com.webmanager.service;

import com.webmanager.dto.EmployeeRequestDTO;
import com.webmanager.dto.EmployeeResponseDTO;
import com.webmanager.dto.PageResponseDTO;
import com.webmanager.entity.Employee;
import com.webmanager.exception.EmailAlreadyExistsExecption;
import com.webmanager.exception.EmployeeNotFoundExecption;
import com.webmanager.mapper.EmployeeMapper;
import com.webmanager.mapper.PageMapper;
import com.webmanager.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;

    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO employee){
        if (repository.existsByEmail(employee.email())){
            throw new EmailAlreadyExistsExecption("Email já cadastrado.");
        }

        Employee savedEmployee = repository.save(mapper.toEntity(employee));

        return mapper.toResponse(savedEmployee);
    }

    public PageResponseDTO<EmployeeResponseDTO> listAllEmployees(Pageable pageable){
        var page = repository.findAll(pageable)
                .map(mapper::toResponse);
        return PageMapper.toPageResponse(page);
    }

    public EmployeeResponseDTO findByEmail(String email) {
        var employee = repository.findByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundExecption("Funcionário não encontrado."));

        return mapper.toResponse(employee);
    }
}
