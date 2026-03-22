package com.philomath.repository;

import com.philomath.entity.FileMetadata;
import com.philomath.entity.FileMetadataStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    /**
     * Find records with given status, ordered by id, limited by pageable.
     * Uses PESSIMISTIC_WRITE (SELECT ... FOR UPDATE) to claim exclusively.
     * Caller must update status to PENDING_VALIDATION and flush within same transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM FileMetadata m WHERE m.status = :status ORDER BY m.id ASC")
    List<FileMetadata> findByStatusForClaim(@Param("status") FileMetadataStatus status, Pageable pageable);
}
