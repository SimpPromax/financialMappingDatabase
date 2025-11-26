package com.JavaWebToken.jwtAuthentication.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "excel_elements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelElement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long elementId;

    @Column(nullable = false, length = 100)
    private String excelElement;

    @Column(length = 500)
    private String exelCellValue;

    @Column(name = "cell_reference")
    private String cellReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sheet_id", nullable = false)
    private ExcelSheet sheet;


}