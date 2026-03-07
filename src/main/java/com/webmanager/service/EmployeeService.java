package com.webmanager.service;

import com.webmanager.dto.EmployeeRequestDTO;
import com.webmanager.dto.EmployeeResponseDTO;
import com.webmanager.entity.Employee;
import com.webmanager.exception.EmailAlreadyExistsExecption;
import com.webmanager.exception.EmployeeNotFoundExecption;
import com.webmanager.mapper.EmployeeMapper;
import com.webmanager.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repository;

    @Autowired
    private EmployeeMapper mapper;

    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO employee){
        if (repository.existsByEmail(employee.email())){
            throw new EmailAlreadyExistsExecption("Email já cadastrado.");
        }

        Employee savedEmployee = repository.save(mapper.toEntity(employee));

        return mapper.toResponse(savedEmployee);
    }

    public List<EmployeeResponseDTO> listAllEmployees(){
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public EmployeeResponseDTO findByEmail(String email) {
        var employee = repository.findByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundExecption("Funcionário não encontrado."));

        return mapper.toResponse(employee);
    }
}
