package com.webmanager.mapper;

import com.webmanager.dto.manager.ManagerRequestDTO;
import com.webmanager.dto.manager.ManagerResponseDTO;
import com.webmanager.entity.Manager;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ManagerMapper {

    @Mapping(target = "password", ignore = true)
    Manager toEntity(ManagerRequestDTO dto);

    ManagerResponseDTO toResponse(Manager manager);
}
