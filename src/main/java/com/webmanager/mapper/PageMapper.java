package com.webmanager.mapper;

import com.webmanager.dto.PageResponseDTO;
import org.springframework.data.domain.Page;

public class PageMapper {

    public static <T> PageResponseDTO<T> toPageResponse(Page<T> page) {
        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}