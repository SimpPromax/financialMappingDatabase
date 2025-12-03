package com.JavaWebToken.jwtAuthentication.dto;

import lombok.Data;
import java.util.List;

@Data
public class CoaResponseDTO {
    private List<CoaDTO> coas;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private boolean hasNext;
    private boolean hasPrevious;

    public CoaResponseDTO(List<CoaDTO> coas, int currentPage, int totalPages, long totalItems) {
        this.coas = coas;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.hasNext = currentPage < totalPages;
        this.hasPrevious = currentPage > 1;
    }
}