package com.JavaWebToken.jwtAuthentication.service;

import com.JavaWebToken.jwtAuthentication.dto.CoaVersionDTO;
import com.JavaWebToken.jwtAuthentication.entity.Coa;
import com.JavaWebToken.jwtAuthentication.entity.CoaVersion;
import com.JavaWebToken.jwtAuthentication.repository.CoaVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoaVersionService {

    private final CoaVersionRepository coaVersionRepository;

    @Transactional
    public CoaVersion createVersion(Coa coa, String changedBy, String changeType, String changes) {
        // Get current max version for this COA
        Integer currentMaxVersion = coaVersionRepository.findMaxVersionByCoaId(coa.getCoaId())
                .orElse(0);

        CoaVersion version = CoaVersion.builder()
                .coa(coa)
                .versionNumber(currentMaxVersion + 1)
                .coaCode(coa.getCoaCode())
                .coaName(coa.getCoaName())
                .description(coa.getDescription())
                .sqlScript(coa.getSqlScript())
                .changedBy(changedBy)
                .changeType(changeType)
                .changes(changes)
                .createdAt(LocalDateTime.now())
                .build();

        return coaVersionRepository.save(version);
    }

    public List<CoaVersion> getVersionsByCoaId(Long coaId) {
        return coaVersionRepository.findByCoaCoaIdOrderByVersionNumberDesc(coaId);
    }

    public List<CoaVersionDTO> getVersionDTOsByCoaId(Long coaId) {
        List<CoaVersion> versions = getVersionsByCoaId(coaId);
        return versions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private CoaVersionDTO convertToDTO(CoaVersion version) {
        CoaVersionDTO dto = new CoaVersionDTO();
        dto.setVersionId(version.getVersionId());
        dto.setCoaId(version.getCoa().getCoaId());
        dto.setVersionNumber(version.getVersionNumber());
        dto.setCoaCode(version.getCoaCode());
        dto.setCoaName(version.getCoaName());
        dto.setDescription(version.getDescription());
        dto.setSqlScript(version.getSqlScript());
        dto.setChangedBy(version.getChangedBy());
        dto.setChangeType(version.getChangeType());
        dto.setChanges(version.getChanges());
        dto.setCreatedAt(version.getCreatedAt());
        return dto;
    }

    public Optional<CoaVersion> getVersion(Long coaId, Integer versionNumber) {
        return coaVersionRepository.findByCoaCoaIdAndVersionNumber(coaId, versionNumber);
    }
}