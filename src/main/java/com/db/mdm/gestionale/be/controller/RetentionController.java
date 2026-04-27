package com.db.mdm.gestionale.be.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.mdm.gestionale.be.service.RetentionService;
import com.db.mdm.gestionale.be.service.WebSocketService;
import com.db.mdm.gestionale.be.utils.Constants;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/retention")
@RequiredArgsConstructor
public class RetentionController {

    private final RetentionService retentionService;
    private final WebSocketService wsService;

    public static record RetentionRequest(
            List<String> entities,
            LocalDate beforeDate,
            boolean includeCompletedAssegnazioniBeforeDate,
            boolean includeOldAllegati,
            boolean includeReadNotificheBeforeDate
    ) {}
    public static record SpaceUsageResponse(String dbSize, String storageSize) {}

    @PostMapping("/run")
    @PreAuthorize("hasRole('ADMIN')")
    public String runRetention(@RequestBody RetentionRequest request) {
        StringBuilder report = new StringBuilder("Pulizia completata:\n");
        List<String> entities = request.entities() == null ? List.of() : request.entities();

        if (entities.contains("assegnazioni")) {
            int count = retentionService.cleanupAssegnazioni(
                    request.beforeDate(), request.includeCompletedAssegnazioniBeforeDate());
            report.append("- Assegnazioni eliminate: ").append(count).append("\n");
        }
        if (entities.contains("cantieri") || entities.contains("commesse")) { // alias legacy
            int count = retentionService.cleanupCantieri();
            report.append("- Cantieri eliminati: ").append(count).append("\n");
        }
        if (entities.contains("clienti")) {
            int count = retentionService.cleanupClienti();
            report.append("- Clienti eliminati: ").append(count).append("\n");
        }
        if (entities.contains("utenti")) {
            int count = retentionService.cleanupUtenti();
            report.append("- Utenze eliminate: ").append(count).append("\n");
        }
        if (entities.contains("veicoli")) {
            int count = retentionService.cleanupVeicoli();
            report.append("- Veicoli eliminati: ").append(count).append("\n");
        }
        if (entities.contains("allegati")) {
            int count = retentionService.cleanupAllegati(
                    request.beforeDate(), request.includeOldAllegati());
            report.append("- Allegati eliminati: ").append(count).append("\n");
        }
        if (entities.contains("notifiche")) {
            int count = retentionService.cleanupNotifiche(
                    request.beforeDate(), request.includeReadNotificheBeforeDate());
            report.append("- Notifiche eliminate: ").append(count).append("\n");
        }

        wsService.broadcast(Constants.MSG_ENTITY_CHANGED, "{\"entity\":\"retention\",\"action\":\"run\",\"id\":null}");
        return report.toString();
    }

    @GetMapping("/space")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public SpaceUsageResponse getCurrentSpaceUsage() {
        return retentionService.getCurrentSpaceUsage();
    }
}
