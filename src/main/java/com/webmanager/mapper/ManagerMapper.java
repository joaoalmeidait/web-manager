package com.webmanager.mapper;

import com.webmanager.dto.ManagerRequestDTO;
import com.webmanager.dto.ManagerResponseDTO;
import com.webmanager.entity.Manager;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ManagerMapper {

    Manager toEntity(ManagerRequestDTO dto);

    ManagerResponseDTO toResponse(Manager manager);
}
