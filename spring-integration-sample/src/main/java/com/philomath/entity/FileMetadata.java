package com.philomath.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "file_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "last_modified", nullable = false)
    private Instant lastModified;

    @Column(nullable = false, length = 512)
    private String filename;

    @Column(name = "remote_path", length = 1024)
    private String remotePath;

    @Column(name = "local_path", nullable = false, length = 1024)
    private String localPath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "retrieved_at", nullable = false)
    private Instant retrievedAt;

    @Enumerated(STRING)
    @Column(nullable = false, length = 32)
    private FileMetadataStatus status;

    @Column(name = "error_message", length = 2048)
    private String errorMessage;
}
