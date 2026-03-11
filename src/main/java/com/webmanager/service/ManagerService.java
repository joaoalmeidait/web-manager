package com.webmanager.service;

import com.webmanager.dto.PageResponseDTO;
import com.webmanager.dto.manager.ManagerRequestDTO;
import com.webmanager.dto.manager.ManagerResponseDTO;
import com.webmanager.entity.Manager;
import com.webmanager.mapper.ManagerMapper;
import com.webmanager.mapper.PageMapper;
import com.webmanager.repository.ManagerRepository;
import com.webmanager.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository repository;
    private final ManagerMapper mapper;
    private final ValidationUtils validationUtils;

    public ManagerResponseDTO createManager(ManagerRequestDTO dto){

        validationUtils.validateCPF(dto.cpf(), () -> repository.existsByCpf(dto.cpf()));
        validationUtils.validateUniqueEmail(dto.email(), () -> repository.existsByEmail(dto.email()));

        Manager saved = repository.save(mapper.toEntity(dto));
        return mapper.toResponse(saved);
    }

    public PageResponseDTO<ManagerResponseDTO> listAllManagers(Pageable pageable) {
        var page = repository.findAll(pageable)
                .map(mapper::toResponse);

        return PageMapper.toPageResponse(page);
    }
}
