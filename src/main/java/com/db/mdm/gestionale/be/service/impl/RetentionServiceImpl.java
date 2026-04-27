package com.db.mdm.gestionale.be.service.impl;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.db.mdm.gestionale.be.controller.RetentionController.SpaceUsageResponse;
import com.db.mdm.gestionale.be.service.RetentionService;
import com.db.mdm.gestionale.be.service.SupabaseS3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RetentionServiceImpl implements RetentionService {

    private final SupabaseS3Service s3Service;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public int cleanupAssegnazioni(LocalDate beforeDate, boolean includeCompletedBeforeDate) {
        if (includeCompletedBeforeDate && beforeDate != null) {
            LocalDateTime cutoff = beforeDate.atStartOfDay();
            return jdbcTemplate.update(
                    """
                    delete from assegnazione
                    where is_deleted = true
                       or end_at < ?
                    """,
                    Timestamp.valueOf(cutoff));
        }
        return jdbcTemplate.update("delete from assegnazione where is_deleted = true");
    }

    @Override
    public int cleanupCantieri() {
        return jdbcTemplate.update(
                """
                delete from cantiere c
                where c.is_deleted = true
                """);
    }

    @Override
    public int cleanupClienti() {
        return jdbcTemplate.update(
                """
                delete from cliente c
                where c.is_deleted = true
                """);
    }

    @Override
    public int cleanupUtenti() {
        return jdbcTemplate.update(
                """
                delete from utente u
                where u.is_deleted = true
                """);
    }

    @Override
    public int cleanupVeicoli() {
        return jdbcTemplate.update(
                """
                delete from veicolo v
                where v.is_deleted = true
                """);
    }

    @Override
    public int cleanupAllegati(LocalDate beforeDate, boolean includeOldActive) {
        List<String> toDeleteFromStorage;
        int deleted;

        if (includeOldActive && beforeDate != null) {
            LocalDateTime cutoff = beforeDate.atStartOfDay();
            toDeleteFromStorage = jdbcTemplate.queryForList(
                    """
                    select storage_path from allegato
                    where is_deleted = true
                       or created_at < ?
                    """,
                    String.class,
                    Timestamp.valueOf(cutoff));

            deleted = jdbcTemplate.update(
                    """
                    delete from allegato
                    where is_deleted = true
                       or created_at < ?
                    """,
                    Timestamp.valueOf(cutoff));
        } else {
            toDeleteFromStorage = jdbcTemplate.queryForList(
                    "select storage_path from allegato where is_deleted = true",
                    String.class);
            deleted = jdbcTemplate.update("delete from allegato where is_deleted = true");
        }

        for (String storagePath : toDeleteFromStorage) {
            try {
                s3Service.deleteFile(storagePath);
            } catch (Exception ignored) {
                // Non blocca la pulizia DB se un file non viene trovato a storage.
            }
        }

        return deleted;
    }

    @Override
    public int cleanupNotifiche(LocalDate beforeDate, boolean includeReadBeforeDate) {
        if (includeReadBeforeDate && beforeDate != null) {
            LocalDateTime cutoff = beforeDate.atStartOfDay();
            return jdbcTemplate.update(
                    """
                    delete from notifica
                    where is_deleted = true
                       or (letta = true and created_at < ?)
                    """,
                    Timestamp.valueOf(cutoff));
        }
        return jdbcTemplate.update("delete from notifica where is_deleted = true");
    }

    @Override
    public SpaceUsageResponse getCurrentSpaceUsage() {
        String dbSize = jdbcTemplate.queryForObject(
                "SELECT pg_size_pretty(pg_database_size(current_database()))",
                String.class
        );
        String storageSize = s3Service.getTotalStorageUsagePretty();
        return new SpaceUsageResponse(dbSize, storageSize);
    }
}
