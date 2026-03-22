package com.philomath.entity;

/**
 * Status for file metadata records.
 * Flow: PENDING_DOWNLOAD -> DOWNLOADED | DOWNLOAD_FAILED -> PENDING_VALIDATION -> VALIDATED | VALIDATION_FAILED
 */
public enum FileMetadataStatus {

    /** Downloaded successfully, in success folder, ready for validation. */
    DOWNLOADED,

    /** Claimed by a validation thread; validation in progress. Prevents concurrent fetch. */
    PENDING_VALIDATION,

    /** Download or move to success folder failed; file in failed folder. */
    DOWNLOAD_FAILED,

    /** Fixed-length validation passed. */
    VALIDATED,

    /** Fixed-length validation failed. */
    VALIDATION_FAILED
}
