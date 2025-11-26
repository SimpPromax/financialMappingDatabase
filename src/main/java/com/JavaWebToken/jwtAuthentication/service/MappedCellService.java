package com.JavaWebToken.jwtAuthentication.service;

import com.JavaWebToken.jwtAuthentication.entity.MappedCellInfo;
import com.JavaWebToken.jwtAuthentication.repository.MappedCellInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MappedCellService {

    @Autowired
    private MappedCellInfoRepository mappedCellInfoRepository;

    /**
     * Save a new mapping between Excel element and COA
     */
    public MappedCellInfo saveMapping(MappedCellInfo mapping) {
        // Additional validation can be added here (e.g., check duplicates)
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
     * Fetch mapping by elementId and sheetId (optional utility)
     */
    public Optional<MappedCellInfo> getMapping(Long sheetId, Long elementId) {
        return mappedCellInfoRepository.findBySheetIdAndElementId(sheetId, elementId);
    }

    /**
     * Delete a mapping by ID
     */
    public void deleteMapping(Long mappingId) {
        mappedCellInfoRepository.deleteById(mappingId);
    }
}
