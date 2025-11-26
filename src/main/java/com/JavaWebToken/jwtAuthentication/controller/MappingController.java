package com.JavaWebToken.jwtAuthentication.controller;



import com.JavaWebToken.jwtAuthentication.dto.MappingDTO;
import com.JavaWebToken.jwtAuthentication.entity.MappedCellInfo;
import com.JavaWebToken.jwtAuthentication.service.MappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mappings")
@RequiredArgsConstructor
public class MappingController {

    private final MappingService mappingService;

    @PostMapping
    public ResponseEntity<MappingDTO> createMapping(@RequestBody MappingDTO dto) {
        // basic validation
        if (dto.getSheetId() == null || dto.getElementId() == null || dto.getCoaId() == null) {
            return ResponseEntity.badRequest().build();
        }

        MappedCellInfo m = MappedCellInfo.builder()
                .sheetId(dto.getSheetId())
                .elementId(dto.getElementId())
                .coaId(dto.getCoaId())
                .createdBy(dto.getCreatedBy())
                .build();

        try {
            MappedCellInfo saved = mappingService.createMapping(m);
            dto.setMappingId(saved.getMappingId());
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<MappingDTO>> listMappings(@RequestParam(value = "sheetId", required = false) Long sheetId) {
        List<MappedCellInfo> list;
        if (sheetId != null) {
            list = mappingService.getMappingsBySheet(sheetId);
        } else {
            list = mappingService.listAll();
        }

        List<MappingDTO> dtos = list.stream().map(m -> {
            MappingDTO d = new MappingDTO();
            d.setMappingId(m.getMappingId());
            d.setSheetId(m.getSheetId());
            d.setElementId(m.getElementId());
            d.setCoaId(m.getCoaId());
            d.setCreatedBy(m.getCreatedBy());
            return d;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}

