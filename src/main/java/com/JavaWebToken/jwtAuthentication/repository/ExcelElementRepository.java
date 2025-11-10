package com.JavaWebToken.jwtAuthentication.repository;

import com.JavaWebToken.jwtAuthentication.entity.ExcelElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExcelElementRepository extends JpaRepository<ExcelElement, Long> {
    void deleteBySheet_SheetId(Long sheetId);
    List<ExcelElement> findBySheet_SheetIdOrderByExcelElement(Long sheetId);
}