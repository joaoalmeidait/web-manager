package com.webmanager.controller;

import com.webmanager.dto.PageResponseDTO;
import com.webmanager.dto.manager.ManagerRequestDTO;
import com.webmanager.dto.manager.ManagerResponseDTO;
import com.webmanager.service.ManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/managers")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService service;

    @PostMapping
    public ResponseEntity<ManagerResponseDTO> createManager(@Valid @RequestBody ManagerRequestDTO dto) {
        var manager = service.createManager(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(manager);
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<ManagerResponseDTO>> listAllManagers(@ParameterObject Pageable pageable) {
        var managers = service.listAllManagers(pageable);
        return ResponseEntity.ok(managers);
    }
}
