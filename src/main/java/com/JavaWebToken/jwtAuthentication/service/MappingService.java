package com.JavaWebToken.jwtAuthentication.service;



import com.JavaWebToken.jwtAuthentication.entity.ExcelElement;
import com.JavaWebToken.jwtAuthentication.entity.ExcelSheet;
import com.JavaWebToken.jwtAuthentication.entity.MappedCellInfo;
import com.JavaWebToken.jwtAuthentication.repository.ExcelElementRepository;
import com.JavaWebToken.jwtAuthentication.repository.ExcelSheetRepository;
import com.JavaWebToken.jwtAuthentication.repository.MappedCellInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MappingService {

    private final MappedCellInfoRepository mappedCellInfoRepository;
    private final ExcelSheetRepository excelSheetRepository;
    private final ExcelElementRepository excelElementRepository;

    public MappedCellInfo createMapping(MappedCellInfo mapping) {
        // Basic validation: sheet exists, element exists
        Optional<ExcelSheet> sheetOpt = excelSheetRepository.findById(mapping.getSheetId());
        if (sheetOpt.isEmpty()) {
            throw new IllegalArgumentException("Sheet not found with id: " + mapping.getSheetId());
        }

        Optional<ExcelElement> elemOpt = excelElementRepository.findById(mapping.getElementId());
        if (elemOpt.isEmpty()) {
            throw new IllegalArgumentException("Excel element not found with id: " + mapping.getElementId());
        }

        // optional: ensure element belongs to sheet
        ExcelElement element = elemOpt.get();
        if (!element.getSheet().getSheetId().equals(mapping.getSheetId())) {
            throw new IllegalArgumentException("Excel element does not belong to provided sheet.");
        }


        mapping.setCreatedDate(LocalDateTime.now());
        mapping.setModifiedDate(LocalDateTime.now());

        return mappedCellInfoRepository.save(mapping);
    }

    public List<MappedCellInfo> getMappingsBySheet(Long sheetId) {
        return mappedCellInfoRepository.findBySheetId(sheetId);
    }

    public List<MappedCellInfo> listAll() {
        return mappedCellInfoRepository.findAll();
    }
}

