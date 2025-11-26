package com.JavaWebToken.jwtAuthentication.repository;

import com.JavaWebToken.jwtAuthentication.entity.MappedCellInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MappedCellInfoRepository extends JpaRepository<MappedCellInfo, Long> {

    List<MappedCellInfo> findBySheetId(Long sheetId);

    Optional<MappedCellInfo> findBySheetIdAndElementId(Long sheetId, Long elementId);

}
