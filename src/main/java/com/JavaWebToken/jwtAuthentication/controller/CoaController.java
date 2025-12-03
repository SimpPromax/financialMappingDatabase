package com.JavaWebToken.jwtAuthentication.controller;

import com.JavaWebToken.jwtAuthentication.dto.*;
import com.JavaWebToken.jwtAuthentication.entity.Coa;
import com.JavaWebToken.jwtAuthentication.service.CoaService;
import com.JavaWebToken.jwtAuthentication.service.CoaVersionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coa")
@RequiredArgsConstructor
@Validated
public class CoaController {

    private final CoaService coaService;
    private final CoaVersionService coaVersionService;
    private final DataSource dataSource;

    // ------------------ CRUD ------------------

    @PostMapping
    public ResponseEntity<CoaDTO> create(@Valid @RequestBody CoaCreateDTO createDTO) {
        Coa coa = Coa.builder()
                .coaCode(createDTO.getCoaCode())
                .coaName(createDTO.getCoaName())
                .description(createDTO.getDescription())
                .sqlScript(createDTO.getSqlScript())
                .createdBy(createDTO.getCreatedBy() != null ? createDTO.getCreatedBy() : "system")
                .build();

        Coa saved = coaService.createCoa(coa);
        return ResponseEntity.ok(CoaDTO.fromEntity(saved));
    }

    @GetMapping
    public ResponseEntity<List<CoaDTO>> list() {
        List<Coa> list = coaService.listActive();
        List<CoaDTO> dtos = list.stream()
                .map(CoaDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/archived")
    public ResponseEntity<List<CoaDTO>> listArchived() {
        List<Coa> list = coaService.listArchived();
        List<CoaDTO> dtos = list.stream()
                .map(CoaDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoaDTO> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody CoaUpdateDTO updateDTO) {

        Coa updated = Coa.builder()
                .coaCode(updateDTO.getCoaCode())
                .coaName(updateDTO.getCoaName())
                .description(updateDTO.getDescription())
                .sqlScript(updateDTO.getSqlScript())
                .modifiedBy(updateDTO.getModifiedBy() != null ? updateDTO.getModifiedBy() : "system")
                .build();

        Coa saved;
        try {
            saved = coaService.updateCoa(id, updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(CoaDTO.fromEntity(saved));
    }

    // ------------------ Archive/Restore ------------------

    @PostMapping("/{id}/archive")
    public ResponseEntity<CoaDTO> archive(
            @PathVariable("id") Long id,
            @Valid @RequestBody CoaArchiveDTO archiveDTO) {

        try {
            // Verify active COA exists
            coaService.findActiveById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Active COA not found with id: " + id));

            Coa archived = coaService.archiveCoa(id, archiveDTO.getArchivedBy());
            return ResponseEntity.ok(CoaDTO.fromEntity(archived));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<CoaDTO> restore(
            @PathVariable("id") Long id,
            @Valid @RequestBody CoaRestoreDTO restoreDTO) {

        try {
            Coa restored = coaService.restoreCoa(id, restoreDTO.getRestoredBy());
            return ResponseEntity.ok(CoaDTO.fromEntity(restored));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ------------------ Version History ------------------
    @GetMapping("/{coaId}/versions")
    public ResponseEntity<List<CoaVersionDTO>> getVersions(@PathVariable Long coaId) {
        try {
            // Verify COA exists
            coaService.findById(coaId)
                    .orElseThrow(() -> new IllegalArgumentException("COA not found"));

            List<CoaVersionDTO> versions = coaVersionService.getVersionDTOsByCoaId(coaId);
            return ResponseEntity.ok(versions);

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

    // ------------------ Search ------------------
    @GetMapping("/search")
    public ResponseEntity<List<CoaDTO>> search(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "false") boolean includeArchived) {

        List<Coa> results;
        if (includeArchived) {
            // Search both active and archived
            results = coaService.listAll().stream()
                    .filter(c -> c.getCoaCode().toLowerCase().contains(query.toLowerCase()) ||
                            c.getCoaName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            // Search only active
            results = coaService.listActive().stream()
                    .filter(c -> c.getCoaCode().toLowerCase().contains(query.toLowerCase()) ||
                            c.getCoaName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }

        List<CoaDTO> dtos = results.stream()
                .map(CoaDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}