package com.db.mdm.gestionale.be.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.mdm.gestionale.be.entity.Impostazioni;
import com.db.mdm.gestionale.be.service.ImpostazioniService;

@RestController
@RequestMapping("/api/impostazioni")
public class ImpostazioniController {

    private final ImpostazioniService service;

    public ImpostazioniController(ImpostazioniService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public List<Impostazioni> getAll() {
        return service.findAll();
    }

    @GetMapping("/{chiave}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public ResponseEntity<Impostazioni> getByChiave(@PathVariable String chiave) {
        return service.findByChiave(chiave)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Impostazioni create(@RequestBody Impostazioni Impostazioni) {
        return service.save(Impostazioni);
    }

    @PutMapping("/{chiave}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Impostazioni> update(@PathVariable String chiave, @RequestBody String valore) {
        Impostazioni updated = service.update(chiave, valore);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
}
