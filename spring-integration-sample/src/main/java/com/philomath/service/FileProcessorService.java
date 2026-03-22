package com.philomath.service;

import com.philomath.entity.FileMetadata;
import com.philomath.entity.FileMetadataStatus;
import com.philomath.repository.FileMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.FileHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

/**
 * Processes files from SFTP: moves to success/failure folders and saves metadata to DB.
 * Success: nfs-base-path/success. Failure: nfs-base-path/failed.
 * Runs in parallel via ExecutorChannel dispatcher.
 */
@Slf4j
@Service
public class FileProcessorService {

    private static final String SUCCESS_SUBDIR = "success";
    private static final String FAILED_SUBDIR = "failed";

    private final FileMetadataRepository fileMetadataRepository;
    private final SftpRemoteFileService sftpRemoteFileService;
    private final String configuredRemoteDirectory;
    private final Path successPath;
    private final Path failedPath;

    public FileProcessorService(
            FileMetadataRepository fileMetadataRepository,
            SftpRemoteFileService sftpRemoteFileService,
            @Value("${sftp.nfs-base-path:./nfs-output}") String nfsBasePath,
            @Value("${sftp.remote-directory:/}") String configuredRemoteDirectory) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.sftpRemoteFileService = sftpRemoteFileService;
        this.configuredRemoteDirectory = configuredRemoteDirectory != null ? configuredRemoteDirectory : "/";
        Path base = Paths.get(nfsBasePath).toAbsolutePath();
        this.successPath = base.resolve(SUCCESS_SUBDIR);
        this.failedPath = base.resolve(FAILED_SUBDIR);
    }

    public void processFile(Message<?> message) {
        Object payload = message.getPayload();
        if (!(payload instanceof File sourceFile)) {
            log.warn("Unexpected payload type: {}", payload != null ? payload.getClass().getName() : "null");
            return;
        }
        String filename = sourceFile.getName();
        String remotePath = message.getHeaders().get(FileHeaders.REMOTE_DIRECTORY, String.class);
        if (remotePath == null || remotePath.isBlank()) {
            remotePath = configuredRemoteDirectory;
        }

        try {
            Files.createDirectories(successPath);
            Path targetPath = successPath.resolve(filename);
            Files.move(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            FileMetadata metadata = FileMetadata.builder()
                    .filename(filename)
                    .remotePath(remotePath)
                    .localPath(targetPath.toAbsolutePath().toString())
                    .fileSize(sourceFile.length())
                    .retrievedAt(Instant.now())
                    .status(FileMetadataStatus.DOWNLOADED)
                    .build();
            fileMetadataRepository.save(metadata);

            sftpRemoteFileService.moveToProcessed(remotePath, filename);

            log.info("Processed file: {} -> {}", filename, targetPath);
        } catch (IOException ioException) {
            log.error("Failed to process file: {}", filename, ioException);
            moveToFailedAndSaveMetadata(sourceFile, filename, remotePath, ioException.getMessage());
        } catch (Exception exception) {
            log.error("Unexpected error processing file: {}", filename, exception);
            moveToFailedAndSaveMetadata(sourceFile, filename, remotePath, exception.getMessage());
        }
    }

    private void moveToFailedAndSaveMetadata(File sourceFile, String filename, String remotePath, String errorMessage) {
        try {
            Files.createDirectories(failedPath);
            Path failedFilePath = failedPath.resolve(filename);
            Files.move(sourceFile.toPath(), failedFilePath, StandardCopyOption.REPLACE_EXISTING);
            saveFailedMetadata(filename, remotePath, failedFilePath.toAbsolutePath().toString(), errorMessage);
            sftpRemoteFileService.moveToFailed(remotePath, filename);
        } catch (IOException moveEx) {
            log.error("Could not move failed file to failed folder: {}", filename, moveEx);
            saveFailedMetadata(filename, remotePath, "(could not move)", errorMessage);
            sftpRemoteFileService.moveToFailed(remotePath, filename);
        }
    }

    private void saveFailedMetadata(String filename, String remotePath, String localPath, String errorMessage) {
        try {
            FileMetadata metadata = FileMetadata.builder()
                    .filename(filename)
                    .remotePath(remotePath)
                    .localPath(localPath)
                    .fileSize(null)
                    .retrievedAt(Instant.now())
                    .status(FileMetadataStatus.DOWNLOAD_FAILED)
                    .errorMessage(errorMessage != null ? truncate(errorMessage, 2048) : null)
                    .build();
            fileMetadataRepository.save(metadata);
        } catch (Exception saveException) {
            log.error("Failed to save error metadata for file: {}", filename, saveException);
        }
    }

    private static String truncate(String value, int maxLength) {
        return value != null && value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
