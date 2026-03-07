package com.webmanager.controller;

import com.webmanager.dto.ManagerRequestDTO;
import com.webmanager.dto.ManagerResponseDTO;
import com.webmanager.service.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/managers")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService service;

    @PostMapping
    public ResponseEntity<ManagerResponseDTO> createManager (@RequestBody ManagerRequestDTO dto){
        var manager = service.createManager(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(manager);
    }
}
