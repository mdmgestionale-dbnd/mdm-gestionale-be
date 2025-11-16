package com.db.mdm.gestionale.be.service;

import com.db.mdm.gestionale.be.controller.RetentionController.SpaceUsageResponse;

public interface RetentionService {
    int cleanupAssegnazioni();
    int cleanupCommesse();
    int cleanupClienti();
    int cleanupUtenti();
    SpaceUsageResponse getCurrentSpaceUsage();
}
