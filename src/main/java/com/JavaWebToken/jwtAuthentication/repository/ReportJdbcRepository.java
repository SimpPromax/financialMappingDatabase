package com.JavaWebToken.jwtAuthentication.repository;

import com.JavaWebToken.jwtAuthentication.dto.CellMappingDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<CellMappingDTO> getCellMappings(String sheetName) {
        String sql = """
            SELECT 
                e.exel_cell_value as targetCell,
                e.excel_element as elementName,
                c.coa_name as coaName,
                c.coa_code as coaCode,
                c.sql_script as sqlScript,
                m.mapping_id as mappingId,
                s.excell_sheet_name as sheetName
            FROM excel_elements e
            JOIN mapped_cell_info m ON e.element_id = m.element_id
            JOIN coa c ON m.coa_id = c.coa_id
            JOIN excel_sheets s ON e.sheet_id = s.sheet_id
            WHERE s.excell_sheet_name = ?
            ORDER BY e.exel_cell_value
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new CellMappingDTO(
                        rs.getString("targetCell"),
                        rs.getString("elementName"),
                        rs.getString("coaName"),
                        rs.getString("coaCode"),
                        rs.getString("sqlScript"),
                        rs.getLong("mappingId"),
                        rs.getString("sheetName")
                ), sheetName);
    }

    public List<String> getAllSheetNames() {
        String sql = "SELECT DISTINCT excell_sheet_name FROM excel_sheets ORDER BY excell_sheet_name";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public List<CellMappingDTO> getCellMappingsBySheetId(Long sheetId) {
        String sql = """
            SELECT 
                e.exel_cell_value as targetCell,
                e.excel_element as elementName,
                c.coa_name as coaName,
                c.coa_code as coaCode,
                c.sql_script as sqlScript,
                m.mapping_id as mappingId,
                s.excell_sheet_name as sheetName
            FROM excel_elements e
            JOIN mapped_cell_info m ON e.element_id = m.element_id
            JOIN coa c ON m.coa_id = c.coa_id
            JOIN excel_sheets s ON e.sheet_id = s.sheet_id
            WHERE e.sheet_id = ?
            ORDER BY e.exel_cell_value
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new CellMappingDTO(
                        rs.getString("targetCell"),
                        rs.getString("elementName"),
                        rs.getString("coaName"),
                        rs.getString("coaCode"),
                        rs.getString("sqlScript"),
                        rs.getLong("mappingId"),
                        rs.getString("sheetName")
                ), sheetId);
    }

    public List<CellMappingDTO> getAllCellMappings() {
        String sql = """
            SELECT 
                e.exel_cell_value as targetCell,
                e.excel_element as elementName,
                c.coa_name as coaName,
                c.coa_code as coaCode,
                c.sql_script as sqlScript,
                m.mapping_id as mappingId,
                s.excell_sheet_name as sheetName
            FROM excel_elements e
            JOIN mapped_cell_info m ON e.element_id = m.element_id
            JOIN coa c ON m.coa_id = c.coa_id
            JOIN excel_sheets s ON e.sheet_id = s.sheet_id
            ORDER BY s.excell_sheet_name, e.exel_cell_value
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new CellMappingDTO(
                        rs.getString("targetCell"),
                        rs.getString("elementName"),
                        rs.getString("coaName"),
                        rs.getString("coaCode"),
                        rs.getString("sqlScript"),
                        rs.getLong("mappingId"),
                        rs.getString("sheetName")
                ));
    }

    public List<CellMappingDTO> getCellMappingsBySheetNames(List<String> sheetNames) {
        if (sheetNames == null || sheetNames.isEmpty()) {
            return List.of();
        }

        String inClause = String.join(",", Collections.nCopies(sheetNames.size(), "?"));
        String sql = String.format("""
            SELECT 
                e.exel_cell_value as targetCell,
                e.excel_element as elementName,
                c.coa_name as coaName,
                c.coa_code as coaCode,
                c.sql_script as sqlScript,
                m.mapping_id as mappingId,
                s.excell_sheet_name as sheetName
            FROM excel_elements e
            JOIN mapped_cell_info m ON e.element_id = m.element_id
            JOIN coa c ON m.coa_id = c.coa_id
            JOIN excel_sheets s ON e.sheet_id = s.sheet_id
            WHERE s.excell_sheet_name IN (%s)
            ORDER BY s.excell_sheet_name, e.exel_cell_value
            """, inClause);

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new CellMappingDTO(
                        rs.getString("targetCell"),
                        rs.getString("elementName"),
                        rs.getString("coaName"),
                        rs.getString("coaCode"),
                        rs.getString("sqlScript"),
                        rs.getLong("mappingId"),
                        rs.getString("sheetName")
                ), sheetNames.toArray());
    }
}