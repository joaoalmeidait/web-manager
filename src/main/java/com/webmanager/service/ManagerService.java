package com.webmanager.service;

import com.webmanager.dto.ManagerRequestDTO;
import com.webmanager.dto.ManagerResponseDTO;
import com.webmanager.entity.Manager;
import com.webmanager.mapper.ManagerMapper;
import com.webmanager.repository.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository repository;
    private final ManagerMapper mapper;

    public ManagerResponseDTO createManager(ManagerRequestDTO dto){
        Manager saved = repository.save(mapper.toEntity(dto));
        return mapper.toResponse(saved);
    }

}
