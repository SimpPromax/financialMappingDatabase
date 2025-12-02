package com.JavaWebToken.jwtAuthentication.service;

import com.JavaWebToken.jwtAuthentication.entity.MappedCellInfo;
import com.JavaWebToken.jwtAuthentication.repository.MappedCellInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MappedCellService {

    @Autowired
    private MappedCellInfoRepository mappedCellInfoRepository;

    /**
     * Save a new mapping between Excel element and COA
     */
    public MappedCellInfo saveMapping(MappedCellInfo mapping) {
        return mappedCellInfoRepository.save(mapping);
    }

    /**
     * Create a new mapping with the provided details
     */
    public MappedCellInfo createMapping(Long sheetId, Long elementId, Long coaId, String createdBy) {
        MappedCellInfo mapping = MappedCellInfo.builder()
                .sheetId(sheetId)
                .elementId(elementId)
                .coaId(coaId)
                .createdBy(createdBy)
                .build();

        return mappedCellInfoRepository.save(mapping);
    }

    /**
     * Fetch all mappings
     */
    public List<MappedCellInfo> getAllMappings() {
        return mappedCellInfoRepository.findAll();
    }

    /**
     * Fetch mappings by sheetId
     */
    public List<MappedCellInfo> getMappingsBySheetId(Long sheetId) {
        return mappedCellInfoRepository.findBySheetId(sheetId);
    }

    /**
     * Fetch mapping by elementId and sheetId
     */
    public Optional<MappedCellInfo> getMapping(Long sheetId, Long elementId) {
        return mappedCellInfoRepository.findBySheetIdAndElementId(sheetId, elementId);
    }

    /**
     * Delete a mapping by ID
     */
    public void deleteMapping(Long mappingId) {
        // Check if mapping exists
        Optional<MappedCellInfo> mapping = mappedCellInfoRepository.findById(mappingId);
        if (mapping.isEmpty()) {
            throw new RuntimeException("Mapping not found with id: " + mappingId);
        }

        // Delete the mapping
        mappedCellInfoRepository.deleteById(mappingId);
    }
}