package com.JavaWebToken.jwtAuthentication.controller;

import com.JavaWebToken.jwtAuthentication.dto.CoaDTO;
import com.JavaWebToken.jwtAuthentication.entity.Coa;
import com.JavaWebToken.jwtAuthentication.service.CoaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/api/coa")
@RequiredArgsConstructor
public class CoaController {

    private final CoaService coaService;
    private final DataSource dataSource; // for SQL validation

    // ------------------ CRUD ------------------

    @PostMapping
    public ResponseEntity<CoaDTO> create(@RequestBody CoaDTO dto) {
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
        }).toList();
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        try {
            coaService.findById(id).orElseThrow(() -> new IllegalArgumentException("COA not found"));
            coaService.deleteCoa(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ------------------ SQL Validation ------------------
    @PostMapping("/validate-sql")
    public ResponseEntity<?> validateSql(@RequestBody Map<String, String> body) {
        String sql = body.get("sqlScript");
        if (sql == null || sql.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "SQL is empty"));
        }

        String trimmed = sql.trim().toUpperCase();
        if (!trimmed.startsWith("SELECT")) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "Only SELECT statements are allowed."));
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            Object resultValue = null;
            if (rs.next()) {
                resultValue = rs.getObject(1); // first column of first row
            }

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "value", resultValue
            ));

        } catch (SQLException e) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "error", e.getMessage()
            ));
        }
    }
}
