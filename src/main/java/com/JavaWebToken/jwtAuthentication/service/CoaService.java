package com.JavaWebToken.jwtAuthentication.service;

import com.JavaWebToken.jwtAuthentication.dto.CoaDTO;
import com.JavaWebToken.jwtAuthentication.entity.Coa;
import com.JavaWebToken.jwtAuthentication.repository.CoaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoaService {

    private final CoaRepository coaRepository;
    private final CoaVersionService coaVersionService;

    @Transactional
    public Coa createCoa(Coa coa) {
        coa.setCreatedDate(LocalDateTime.now());
        coa.setModifiedDate(LocalDateTime.now());
        coa.setArchived(false); // Ensure not archived on creation
        Coa savedCoa = coaRepository.save(coa);

        // Create initial version
        String changes = String.format(
                "{\"action\": \"CREATE\", \"coaCode\": \"%s\", \"coaName\": \"%s\", \"description\": \"%s\"}",
                coa.getCoaCode(),
                coa.getCoaName(),
                coa.getDescription() != null ? coa.getDescription() : ""
        );

        coaVersionService.createVersion(savedCoa,
                coa.getCreatedBy() != null ? coa.getCreatedBy() : "system",
                "CREATE",
                changes);

        return savedCoa;
    }

    @Transactional
    public Coa updateCoa(Long id, Coa updated) {
        Optional<Coa> opt = coaRepository.findById(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("COA not found with id: " + id);
        }

        Coa existing = opt.get();

        // Check if archived
        if (Boolean.TRUE.equals(existing.getArchived())) {
            throw new IllegalArgumentException("Cannot update an archived COA");
        }

        StringBuilder changes = new StringBuilder("{");
        boolean hasChanges = false;

        // Track changes
        if (!Objects.equals(existing.getCoaCode(), updated.getCoaCode())) {
            if (hasChanges) changes.append(", ");
            changes.append(String.format("\"coaCode\": {\"old\": \"%s\", \"new\": \"%s\"}",
                    existing.getCoaCode(), updated.getCoaCode()));
            existing.setCoaCode(updated.getCoaCode());
            hasChanges = true;
        }

        if (!Objects.equals(existing.getCoaName(), updated.getCoaName())) {
            if (hasChanges) changes.append(", ");
            changes.append(String.format("\"coaName\": {\"old\": \"%s\", \"new\": \"%s\"}",
                    existing.getCoaName(), updated.getCoaName()));
            existing.setCoaName(updated.getCoaName());
            hasChanges = true;
        }

        if (!Objects.equals(existing.getDescription(), updated.getDescription())) {
            if (hasChanges) changes.append(", ");
            changes.append(String.format("\"description\": {\"old\": \"%s\", \"new\": \"%s\"}",
                    existing.getDescription() != null ? existing.getDescription() : "",
                    updated.getDescription() != null ? updated.getDescription() : ""));
            existing.setDescription(updated.getDescription());
            hasChanges = true;
        }

        if (!Objects.equals(existing.getSqlScript(), updated.getSqlScript())) {
            if (hasChanges) changes.append(", ");
            changes.append("\"sqlScript\": \"updated\"");
            existing.setSqlScript(updated.getSqlScript());
            hasChanges = true;
        }

        if (hasChanges) {
            changes.append("}");
            existing.setModifiedBy(updated.getModifiedBy());
            existing.setModifiedDate(LocalDateTime.now());

            Coa savedCoa = coaRepository.save(existing);

            // Create version record
            coaVersionService.createVersion(savedCoa,
                    updated.getModifiedBy() != null ? updated.getModifiedBy() : "system",
                    "UPDATE",
                    changes.toString());

            return savedCoa;
        }

        // No changes, return existing
        return existing;
    }

    public List<Coa> listAll() {
        return coaRepository.findAll();
    }

    public List<Coa> listActive() {
        return coaRepository.findByArchivedFalse();
    }

    public List<Coa> listArchived() {
        return coaRepository.findByArchivedTrue();
    }

    public Optional<Coa> findById(Long id) {
        return coaRepository.findById(id);
    }

    public Optional<Coa> findActiveById(Long id) {
        return coaRepository.findByCoaIdAndArchivedFalse(id);
    }

    /**
     * Fetch all COA and convert to DTO
     */
    public List<CoaDTO> getAllCoa() {
        return coaRepository.findByArchivedFalse()
                .stream()
                .map(coa -> {
                    CoaDTO dto = new CoaDTO();
                    dto.setCoaId(coa.getCoaId());
                    dto.setCoaCode(coa.getCoaCode());
                    dto.setCoaName(coa.getCoaName());
                    dto.setDescription(coa.getDescription());
                    dto.setSqlScript(coa.getSqlScript());
                    dto.setCreatedBy(coa.getCreatedBy());
                    dto.setArchived(coa.getArchived());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<CoaDTO> getAllArchivedCoa() {
        return coaRepository.findByArchivedTrue()
                .stream()
                .map(coa -> {
                    CoaDTO dto = new CoaDTO();
                    dto.setCoaId(coa.getCoaId());
                    dto.setCoaCode(coa.getCoaCode());
                    dto.setCoaName(coa.getCoaName());
                    dto.setDescription(coa.getDescription());
                    dto.setSqlScript(coa.getSqlScript());
                    dto.setCreatedBy(coa.getCreatedBy());
                    dto.setArchived(coa.getArchived());
                    dto.setArchivedBy(coa.getArchivedBy());
                    dto.setArchivedDate(coa.getArchivedDate());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Coa archiveCoa(Long id, String archivedBy) {
        Optional<Coa> opt = coaRepository.findById(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("COA not found with id: " + id);
        }

        Coa coa = opt.get();

        if (Boolean.TRUE.equals(coa.getArchived())) {
            throw new IllegalArgumentException("COA is already archived");
        }

        // Create version record for archive
        String changes = String.format(
                "{\"action\": \"ARCHIVE\", \"coaCode\": \"%s\", \"coaName\": \"%s\"}",
                coa.getCoaCode(), coa.getCoaName());

        coaVersionService.createVersion(coa, archivedBy, "ARCHIVE", changes);

        // Mark as archived
        coa.setArchived(true);
        coa.setArchivedBy(archivedBy);
        coa.setArchivedDate(LocalDateTime.now());
        coa.setModifiedDate(LocalDateTime.now());

        return coaRepository.save(coa);
    }

    @Transactional
    public Coa restoreCoa(Long id, String restoredBy) {
        Optional<Coa> opt = coaRepository.findById(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("COA not found with id: " + id);
        }

        Coa coa = opt.get();

        if (Boolean.FALSE.equals(coa.getArchived())) {
            throw new IllegalArgumentException("COA is not archived");
        }

        // Create version record for restore
        String changes = String.format(
                "{\"action\": \"RESTORE\", \"coaCode\": \"%s\", \"coaName\": \"%s\"}",
                coa.getCoaCode(), coa.getCoaName());

        coaVersionService.createVersion(coa, restoredBy, "RESTORE", changes);

        // Restore (un-archive)
        coa.setArchived(false);
        coa.setArchivedBy(null);
        coa.setArchivedDate(null);
        coa.setModifiedDate(LocalDateTime.now());

        return coaRepository.save(coa);
    }

    // Hard delete - only for cleanup if needed
    @Transactional
    public void hardDeleteCoa(Long id) {
        coaRepository.findById(id).ifPresent(coa -> {
            // Create a version record before hard deletion
            String changes = String.format(
                    "{\"action\": \"HARD_DELETE\", \"coaCode\": \"%s\", \"coaName\": \"%s\"}",
                    coa.getCoaCode(), coa.getCoaName());

            coaVersionService.createVersion(coa, "system", "HARD_DELETE", changes);

            coaRepository.delete(coa);
        });
    }
}