package com.webmanager.mapper;

import com.webmanager.dto.employee.EmployeeRequestDTO;
import com.webmanager.dto.employee.EmployeeResponseDTO;
import com.webmanager.dto.employee.EmployeeUpdateDTO;
import com.webmanager.entity.Employee;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    Employee toEntity(EmployeeRequestDTO dto);

    @Mapping(source = "manager.name", target = "managerName")
    EmployeeResponseDTO toResponse(Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeFromDto(EmployeeUpdateDTO dto, @MappingTarget Employee employee);
}