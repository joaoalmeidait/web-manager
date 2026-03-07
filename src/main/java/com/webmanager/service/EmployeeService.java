package com.webmanager.service;

import com.webmanager.dto.EmployeeRequestDTO;
import com.webmanager.dto.EmployeeResponseDTO;
import com.webmanager.dto.PageResponseDTO;
import com.webmanager.entity.Employee;
import com.webmanager.entity.Manager;
import com.webmanager.exception.EmailAlreadyExistsExecption;
import com.webmanager.exception.UserNotFound;
import com.webmanager.mapper.EmployeeMapper;
import com.webmanager.mapper.PageMapper;
import com.webmanager.repository.EmployeeRepository;
import com.webmanager.repository.ManagerRepository;
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

    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto){
        validateEmail(dto);

        Manager manager = resolveManager(dto.managerId());

        var entity = mapper.toEntity(dto);
        entity.setManager(manager);

        Employee savedEmployee = repository.save(entity);

        return mapper.toResponse(savedEmployee);
    }

    private void validateEmail(EmployeeRequestDTO dto) {
        if (repository.existsByEmail(dto.email())){
            throw new EmailAlreadyExistsExecption("Email já cadastrado.");
        }
    }

    public PageResponseDTO<EmployeeResponseDTO> listAllEmployees(Pageable pageable){
        var page = repository.findAll(pageable)
                .map(mapper::toResponse);
        return PageMapper.toPageResponse(page);
    }

    public EmployeeResponseDTO findByEmail(String email) {
        var employee = repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("Funcionário não encontrado."));

        return mapper.toResponse(employee);
    }

    private Manager resolveManager(UUID managerId){

        if (managerId != null) {
            return managerRepository.findById(managerId)
                    .orElseThrow(() -> new UserNotFound("Manager não encontrado."));
        }

        return managerRepository.findByName("Sistema")
                .orElseThrow(() -> new UserNotFound("Manager padrão não encontrado."));
    }
}
