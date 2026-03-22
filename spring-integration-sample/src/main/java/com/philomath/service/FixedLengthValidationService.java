package com.philomath.service;

import com.philomath.entity.FileMetadata;
import com.philomath.entity.FileMetadataStatus;
import com.philomath.fixedlength.FixedLengthDataRecord;
import com.philomath.fixedlength.FixedLengthFooter;
import com.philomath.fixedlength.FixedLengthHeader;
import com.philomath.repository.FileMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import name.velikodniy.vitaliy.fixedlength.FixedLength;
import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Validates fixed-length files: header row, data rows, footer row.
 * Uses the fixedlength library.
 */
@Slf4j
@Service
public class FixedLengthValidationService {

    private final FileMetadataRepository fileMetadataRepository;
    private final Validator validator;

    public FixedLengthValidationService(FileMetadataRepository fileMetadataRepository, Validator validator) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.validator = validator;
    }

    public void validate(FileMetadata metadata) {
        String filename = metadata.getFilename();
        String localPath = metadata.getLocalPath();
        if (localPath == null || localPath.isBlank() || "(could not move)".equals(localPath)) {
            markValidationFailed(metadata, "No local path");
            return;
        }

        Path path = Path.of(localPath);
        if (!Files.exists(path)) {
            markValidationFailed(metadata, "File not found: " + localPath);
            return;
        }

        try (FileInputStream is = new FileInputStream(path.toFile())) {
            List<Object> parsed = new FixedLength()
                    .registerLineType(FixedLengthHeader.class)
                    .registerLineType(FixedLengthDataRecord.class)
                    .registerLineType(FixedLengthFooter.class)
                    .skipErroneousLines()
                    .parse(is);

            if (parsed.isEmpty()) {
                markValidationFailed(metadata, "File is empty or failed to parse");
                return;
            }

            Object first = parsed.get(0);
            Object last = parsed.get(parsed.size() - 1);

            if (!(first instanceof FixedLengthHeader header)) {
                markValidationFailed(metadata, "First row is not a header (expected H...)");
                return;
            }
            if (!(last instanceof FixedLengthFooter footer)) {
                markValidationFailed(metadata, "Last row is not a footer (expected F...)");
                return;
            }

            List<FixedLengthDataRecord> dataRecords = parsed.stream()
                    .filter(FixedLengthDataRecord.class::isInstance)
                    .map(FixedLengthDataRecord.class::cast)
                    .toList();

            String patternViolations = validatePatterns(header, dataRecords, footer);
            if (patternViolations != null) {
                markValidationFailed(metadata, "Pattern validation failed: " + patternViolations);
                return;
            }

            int expectedCount = header.recordCount != null ? header.recordCount : 0;
            if (dataRecords.size() != expectedCount) {
                markValidationFailed(metadata,
                        String.format("Record count mismatch: header=%d, actual=%d", expectedCount, dataRecords.size()));
                return;
            }
            if (footer.recordCount != null && footer.recordCount != expectedCount) {
                markValidationFailed(metadata,
                        String.format("Footer record count mismatch: expected=%d, footer=%d", expectedCount, footer.recordCount));
                return;
            }

            BigDecimal total = dataRecords.stream()
                    .map(r -> r.amount != null && !r.amount.isBlank() ? new BigDecimal(r.amount.trim()) : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal footerTotal = footer.totalAmount != null && !footer.totalAmount.isBlank()
                    ? new BigDecimal(footer.totalAmount.trim()) : BigDecimal.ZERO;
            if (total.compareTo(footerTotal) != 0) {
                markValidationFailed(metadata,
                        String.format("Total amount mismatch: calculated=%s, footer=%s", total, footerTotal));
                return;
            }

            metadata.setStatus(FileMetadataStatus.VALIDATED);
            fileMetadataRepository.save(metadata);
            log.info("Validated file: {}", filename);
        } catch (IOException e) {
            markValidationFailed(metadata, "IO error: " + e.getMessage());
            log.error("Failed to validate file: {}", filename, e);
        } catch (Exception e) {
            markValidationFailed(metadata, "Parse error: " + e.getMessage());
            log.error("Failed to validate file: {}", filename, e);
        }
    }

    private void markValidationFailed(FileMetadata metadata, String errorMessage) {
        metadata.setStatus(FileMetadataStatus.VALIDATION_FAILED);
        metadata.setErrorMessage(truncate(errorMessage, 2048));
        fileMetadataRepository.save(metadata);
    }

    private String validatePatterns(FixedLengthHeader header, List<FixedLengthDataRecord> dataRecords, FixedLengthFooter footer) {
        List<String> messages = new java.util.ArrayList<>();
        for (ConstraintViolation<?> v : validator.validate(header)) {
            messages.add("header." + v.getPropertyPath() + ": " + v.getMessage());
        }
        for (int i = 0; i < dataRecords.size(); i++) {
            for (ConstraintViolation<?> v : validator.validate(dataRecords.get(i))) {
                messages.add("record[" + i + "]." + v.getPropertyPath() + ": " + v.getMessage());
            }
        }
        for (ConstraintViolation<?> v : validator.validate(footer)) {
            messages.add("footer." + v.getPropertyPath() + ": " + v.getMessage());
        }
        return messages.isEmpty() ? null : String.join("; ", messages);
    }

    private static String truncate(String value, int maxLength) {
        return value != null && value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
