-- Leader election lock table only (custom schema, no other Spring Integration tables)
-- Table name: leader_LOCK (prefix "leader_" + "LOCK" per DefaultLockRepository convention)
CREATE TABLE IF NOT EXISTS leader_LOCK (
    LOCK_KEY    CHAR(36)    NOT NULL,
    REGION      VARCHAR(100) NOT NULL,
    CLIENT_ID   CHAR(36),
    CREATED_DATE TIMESTAMP  NOT NULL,
    EXPIRED_AFTER TIMESTAMP NOT NULL,
    CONSTRAINT leader_LOCK_PK PRIMARY KEY (LOCK_KEY, REGION)
);

-- File metadata (synced from SFTP, written to NFS)
-- Status flow: PENDING_DOWNLOAD -> DOWNLOADED | DOWNLOAD_FAILED -> PENDING_VALIDATION -> VALIDATED | VALIDATION_FAILED
-- (PENDING_DOWNLOAD/PENDING_VALIDATION are implicit; we store DOWNLOADED while awaiting validation)
CREATE TABLE IF NOT EXISTS file_metadata (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    last_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    filename     VARCHAR(512) NOT NULL,
    remote_path  VARCHAR(1024),
    local_path   VARCHAR(1024) NOT NULL,
    file_size    BIGINT,
    retrieved_at TIMESTAMP NOT NULL,
    status       VARCHAR(32) NOT NULL,
    error_message VARCHAR(2048)
);
