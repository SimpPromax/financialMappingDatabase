package com.JavaWebToken.jwtAuthentication.repository;

import com.JavaWebToken.jwtAuthentication.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    Optional<UploadedFile> findByFileName(String fileName);

    Boolean existsByFileName(String fileName);

    List<UploadedFile> findAllByOrderByDownloadDateDesc();

    void deleteByFileName(String fileName);

    // Add this query method for searching by sheet name
    @Query("SELECT u FROM UploadedFile u WHERE LOWER(u.fileName) LIKE LOWER(CONCAT('%', :sheetName, '%'))")
    List<UploadedFile> findByFileNameContaining(String sheetName);
}