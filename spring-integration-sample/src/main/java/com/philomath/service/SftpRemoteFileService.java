package com.philomath.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Moves remote files on the SFTP server to "processed" or "failed" folders after local processing.
 * Ensures the next poll does not pick up the same file again.
 */
@Slf4j
@Service
public class SftpRemoteFileService {

    private final SessionFactory<?> sessionFactory;
    private final String processedFolderName;
    private final String failedFolderName;

    public SftpRemoteFileService(
            @Qualifier("cachedSftpSessionFactory") SessionFactory<?> sessionFactory,
            @Value("${sftp.remote-processed-directory:processed}") String processedFolderName,
            @Value("${sftp.remote-failed-directory:failed}") String failedFolderName) {
        this.sessionFactory = sessionFactory;
        this.processedFolderName = processedFolderName;
        this.failedFolderName = failedFolderName;
    }

    /**
     * Moves the remote file to the processed folder. Creates the processed directory if it does not exist.
     *
     * @param remoteDirectory the remote directory containing the file (e.g. /upload)
     * @param filename         the file name
     * @return true if move succeeded, false otherwise (logged, does not throw)
     */
    public boolean moveToProcessed(String remoteDirectory, String filename) {
        if (remoteDirectory == null || remoteDirectory.isBlank() || filename == null || filename.isBlank()) {
            log.warn("Cannot move to processed: remoteDirectory or filename is empty");
            return false;
        }
        String baseDir = remoteDirectory.replaceAll("/$", "");
        String sourcePath = baseDir + "/" + filename;
        String processedDir = baseDir + "/" + processedFolderName;
        String destPath = processedDir + "/" + filename;

        try (Session<?> session = sessionFactory.getSession()) {
            if (!session.exists(processedDir)) {
                session.mkdir(processedDir);
                log.debug("Created remote directory: {}", processedDir);
            }
            session.rename(sourcePath, destPath);
            log.info("Moved remote file to processed: {} -> {}", sourcePath, destPath);
            return true;
        } catch (IOException e) {
            log.warn("Failed to move remote file to processed: {} (file already processed locally)", sourcePath, e);
            return false;
        }
    }

    /**
     * Moves the remote file to the failed folder when local processing fails.
     * Creates the failed directory if it does not exist.
     *
     * @param remoteDirectory the remote directory containing the file (e.g. /upload)
     * @param filename        the file name
     * @return true if move succeeded, false otherwise (logged, does not throw)
     */
    public boolean moveToFailed(String remoteDirectory, String filename) {
        if (remoteDirectory == null || remoteDirectory.isBlank() || filename == null || filename.isBlank()) {
            log.warn("Cannot move to failed: remoteDirectory or filename is empty");
            return false;
        }
        String baseDir = remoteDirectory.replaceAll("/$", "");
        String sourcePath = baseDir + "/" + filename;
        String failedDir = baseDir + "/" + failedFolderName;
        String destPath = failedDir + "/" + filename;

        try (Session<?> session = sessionFactory.getSession()) {
            if (!session.exists(failedDir)) {
                session.mkdir(failedDir);
                log.debug("Created remote directory: {}", failedDir);
            }
            session.rename(sourcePath, destPath);
            log.info("Moved remote file to failed: {} -> {}", sourcePath, destPath);
            return true;
        } catch (IOException e) {
            log.warn("Failed to move remote file to failed: {} (file already processed locally)", sourcePath, e);
            return false;
        }
    }
}
