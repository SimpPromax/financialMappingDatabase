package com.JavaWebToken.jwtAuthentication.repository;

import com.JavaWebToken.jwtAuthentication.entity.ExcelSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExcelSheetRepository extends JpaRepository<ExcelSheet, Long> {
    Optional<ExcelSheet> findByExcellSheetName(String sheetName);
}