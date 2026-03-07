package com.webmanager.mapper;

import com.webmanager.dto.EmployeeRequestDTO;
import com.webmanager.dto.EmployeeResponseDTO;
import com.webmanager.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    Employee toEntity(EmployeeRequestDTO dto);

    @Mapping(source = "manager.name", target = "managerName")
    EmployeeResponseDTO toResponse(Employee employee);
}