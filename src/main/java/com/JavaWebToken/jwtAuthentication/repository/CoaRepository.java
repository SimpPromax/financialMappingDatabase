package com.JavaWebToken.jwtAuthentication.repository;

import com.JavaWebToken.jwtAuthentication.entity.Coa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CoaRepository extends JpaRepository<Coa, Long> {

    // Find active (non-archived) COAs
    List<Coa> findByArchivedFalse();

    // Find archived COAs
    List<Coa> findByArchivedTrue();

    // Find active COA by ID - FIXED: Use coaId instead of id
    Optional<Coa> findByCoaIdAndArchivedFalse(Long coaId);

    // Search active COAs by code or name
    @Query("SELECT c FROM Coa c WHERE c.archived = false AND " +
            "(LOWER(c.coaCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.coaName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Coa> searchActive(@Param("query") String query);

    // Search archived COAs by code or name
    @Query("SELECT c FROM Coa c WHERE c.archived = true AND " +
            "(LOWER(c.coaCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.coaName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Coa> searchArchived(@Param("query") String query);

    // Check if COA code exists (active only)
    boolean existsByCoaCodeAndArchivedFalse(String coaCode);

    // Check if COA code exists (including archived)
    boolean existsByCoaCode(String coaCode);

    // Check if COA code exists for a different COA (for update validation)
    boolean existsByCoaCodeAndCoaIdNotAndArchivedFalse(String coaCode, Long coaId);

    // Find by COA code (active only)
    Optional<Coa> findByCoaCodeAndArchivedFalse(String coaCode);

    // Find by COA code (including archived)
    Optional<Coa> findByCoaCode(String coaCode);
}