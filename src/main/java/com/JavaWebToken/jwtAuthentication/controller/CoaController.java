package com.JavaWebToken.jwtAuthentication.controller;



import com.JavaWebToken.jwtAuthentication.dto.CoaDTO;
import com.JavaWebToken.jwtAuthentication.entity.Coa;
import com.JavaWebToken.jwtAuthentication.service.CoaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coa")
@RequiredArgsConstructor
public class CoaController {

    private final CoaService coaService;

    @PostMapping
    public ResponseEntity<CoaDTO> create(@RequestBody CoaDTO dto) {
        // basic validation - ensure sqlScript present
        if (dto.getSqlScript() == null || dto.getSqlScript().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Coa coa = Coa.builder()
                .coaCode(dto.getCoaCode())
                .coaName(dto.getCoaName())
                .description(dto.getDescription())
                .sqlScript(dto.getSqlScript())
                .createdBy(dto.getCreatedBy())
                .build();

        Coa saved = coaService.createCoa(coa);

        dto.setCoaId(saved.getCoaId());
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<CoaDTO>> list() {
        List<Coa> list = coaService.listAll();
        List<CoaDTO> dtos = list.stream().map(c -> {
            CoaDTO d = new CoaDTO();
            d.setCoaId(c.getCoaId());
            d.setCoaCode(c.getCoaCode());
            d.setCoaName(c.getCoaName());
            d.setDescription(c.getDescription());
            d.setSqlScript(c.getSqlScript());
            d.setCreatedBy(c.getCreatedBy());
            return d;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoaDTO> update(@PathVariable("id") Long id, @RequestBody CoaDTO dto) {
        Coa updated = Coa.builder()
                .coaCode(dto.getCoaCode())
                .coaName(dto.getCoaName())
                .description(dto.getDescription())
                .sqlScript(dto.getSqlScript())
                .modifiedBy(dto.getCreatedBy())
                .build();

        Coa saved;
        try {
            saved = coaService.updateCoa(id, updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }

        dto.setCoaId(saved.getCoaId());
        return ResponseEntity.ok(dto);
    }
}

