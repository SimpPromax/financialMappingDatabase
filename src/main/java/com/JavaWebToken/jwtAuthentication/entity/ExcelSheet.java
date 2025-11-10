package com.JavaWebToken.jwtAuthentication.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "excel_sheets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sheetId;

    @Column(nullable = false, length = 50)
    private String excellSheetName;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "created_date")
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    // REMOVED orphanRemoval = true to fix the collection reference issue
    @OneToMany(mappedBy = "sheet", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ExcelElement> excelElements = new ArrayList<>();

    public void addElement(ExcelElement element) {
        excelElements.add(element);
        element.setSheet(this);
    }
}