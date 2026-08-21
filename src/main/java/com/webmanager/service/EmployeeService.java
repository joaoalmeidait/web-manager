package com.webmanager.service;

import com.webmanager.dto.PageResponseDTO;
import com.webmanager.dto.employee.EmployeeRequestDTO;
import com.webmanager.dto.employee.EmployeeResponseDTO;
import com.webmanager.dto.employee.EmployeeUpdateDTO;
import com.webmanager.entity.Employee;
import com.webmanager.entity.Manager;
import com.webmanager.enums.EmployeeRole;
import com.webmanager.exception.UserNotFoundException;
import com.webmanager.mapper.EmployeeMapper;
import com.webmanager.mapper.PageMapper;
import com.webmanager.repository.EmployeeRepository;
import com.webmanager.repository.ManagerRepository;
import com.webmanager.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;
    private final ManagerRepository managerRepository;
    private final ValidationUtils validationUtils;

    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {

        validationUtils.validateCPF(dto.cpf(), () -> repository.existsByCpf(dto.cpf()));
        validationUtils.validateUniqueEmail(dto.email(), () -> repository.existsByEmail(dto.email()));

        Manager manager = resolveManager(dto.managerId());

        var entity = mapper.toEntity(dto);
        entity.setManager(manager);

        Employee savedEmployee = repository.save(entity);

        return mapper.toResponse(savedEmployee);
    }

    public EmployeeResponseDTO updateEmployee(UUID id, EmployeeUpdateDTO dto) {
        Employee employee = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Funcionário não encontrado."));

        if (dto.email() != null) {
            validationUtils.validateUniqueEmail(dto.email(), () -> repository.existsByEmail(dto.email()));
        }

        mapper.updateEmployeeFromDto(dto, employee);

        if (dto.managerId() != null) {
            employee.setManager(resolveManager(dto.managerId()));
        }

        repository.save(employee);

        return mapper.toResponse(employee);
    }

    public PageResponseDTO<EmployeeResponseDTO> listAllEmployees(Pageable pageable, EmployeeRole role){
        var page = (role != null ? repository.findByRole(role, pageable) : repository.findAll(pageable))
                .map(mapper::toResponse);
        return PageMapper.toPageResponse(page);
    }

    public EmployeeResponseDTO findByEmail(String email) {
        var employee = repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Funcionário não encontrado."));

        return mapper.toResponse(employee);
    }

    public EmployeeResponseDTO findById(UUID id) {
        var employee = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Funcionário não encontrado."));

        return mapper.toResponse(employee);
    }

    private Manager resolveManager(UUID managerId){

        if (managerId != null) {
            return managerRepository.findById(managerId)
                    .orElseThrow(() -> new UserNotFoundException("Manager não encontrado."));
        }

        return managerRepository.findByName("Sistema")
                .orElseThrow(() -> new UserNotFoundException("Manager padrão não encontrado."));
    }
}
