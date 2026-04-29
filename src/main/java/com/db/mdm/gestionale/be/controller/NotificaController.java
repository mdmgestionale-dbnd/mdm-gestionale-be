package com.db.mdm.gestionale.be.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.db.mdm.gestionale.be.entity.Notifica;
import com.db.mdm.gestionale.be.service.NotificaService;
import com.db.mdm.gestionale.be.service.UtenteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifiche")
@RequiredArgsConstructor
public class NotificaController {

    private final NotificaService notificaService;
    private final UtenteService utenteService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public List<Notifica> list(@RequestParam(defaultValue = "false") boolean soloNonLette) {
        var current = utenteService.getCurrentUtenteOrNull();
        if (current != null && Integer.valueOf(0).equals(current.getLivello())) {
            notificaService.generaNotificheScadenzeVeicoli(30);
        }
        return notificaService.findAll(soloNonLette);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public ResponseEntity<Notifica> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificaService.markAsRead(id));
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public ResponseEntity<Void> markAllAsRead() {
        notificaService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh/veicoli")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public ResponseEntity<Map<String, Integer>> refreshVeicoli(@RequestParam(defaultValue = "30") int giorni) {
        int generated = notificaService.generaNotificheScadenzeVeicoli(giorni);
        return ResponseEntity.ok(Map.of("generated", generated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificaService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
