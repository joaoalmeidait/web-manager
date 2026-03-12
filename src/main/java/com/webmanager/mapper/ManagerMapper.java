package com.webmanager.mapper;

import com.webmanager.dto.manager.ManagerRequestDTO;
import com.webmanager.dto.manager.ManagerResponseDTO;
import com.webmanager.entity.Manager;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ManagerMapper {

    Manager toEntity(ManagerRequestDTO dto);

    ManagerResponseDTO toResponse(Manager manager);
}
