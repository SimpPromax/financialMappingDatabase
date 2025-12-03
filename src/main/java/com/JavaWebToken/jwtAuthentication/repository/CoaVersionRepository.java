package com.JavaWebToken.jwtAuthentication.repository;

import com.JavaWebToken.jwtAuthentication.entity.CoaVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CoaVersionRepository extends JpaRepository<CoaVersion, Long> {

    List<CoaVersion> findByCoaCoaIdOrderByVersionNumberDesc(Long coaId);

    @Query("SELECT MAX(v.versionNumber) FROM CoaVersion v WHERE v.coa.coaId = :coaId")
    Optional<Integer> findMaxVersionByCoaId(@Param("coaId") Long coaId);

    Optional<CoaVersion> findByCoaCoaIdAndVersionNumber(Long coaId, Integer versionNumber);
}