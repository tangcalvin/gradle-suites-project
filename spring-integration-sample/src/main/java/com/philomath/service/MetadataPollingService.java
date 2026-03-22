package com.philomath.service;

import com.philomath.entity.FileMetadata;
import com.philomath.entity.FileMetadataStatus;
import com.philomath.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Supplies metadata records for validation polling.
 *
 * <p>Claims records using entity-based flow with PESSIMISTIC_WRITE (SELECT ... FOR UPDATE).
 * Locks rows, updates status to PENDING_VALIDATION, and persists via JPA (with @Version).
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MetadataPollingService {

    private final FileMetadataRepository fileMetadataRepository;

    /**
     * Claims records by locking (FOR UPDATE), updating to PENDING_VALIDATION, and persisting.
     * @Version on entity provides optimistic locking on save.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<FileMetadata> pollPendingValidation(int maxPerPoll) {
        List<FileMetadata> locked = fileMetadataRepository.findByStatusForClaim(
                FileMetadataStatus.DOWNLOADED, PageRequest.of(0, maxPerPoll));
        for (FileMetadata m : locked) {
            m.setStatus(FileMetadataStatus.PENDING_VALIDATION);
        }
        return locked;
    }
}
