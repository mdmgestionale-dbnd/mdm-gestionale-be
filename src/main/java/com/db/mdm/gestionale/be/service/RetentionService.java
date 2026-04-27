package com.db.mdm.gestionale.be.service;

import java.time.LocalDate;

import com.db.mdm.gestionale.be.controller.RetentionController.SpaceUsageResponse;

public interface RetentionService {
    int cleanupAssegnazioni(LocalDate beforeDate, boolean includeCompletedBeforeDate);
    int cleanupCantieri();
    int cleanupClienti();
    int cleanupUtenti();
    int cleanupVeicoli();
    int cleanupAllegati(LocalDate beforeDate, boolean includeOldActive);
    int cleanupNotifiche(LocalDate beforeDate, boolean includeReadBeforeDate);
    SpaceUsageResponse getCurrentSpaceUsage();
}
