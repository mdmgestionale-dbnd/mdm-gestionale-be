package com.db.mdm.gestionale.be.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.db.mdm.gestionale.be.entity.Veicolo;
import com.db.mdm.gestionale.be.service.VeicoloService;

@RestController
@RequestMapping("/api/veicolo")
@RequiredArgsConstructor
public class VeicoloController {
    private final VeicoloService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public Veicolo save(@RequestBody Veicolo entity) {
        return service.save(entity);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public List<Veicolo> findAll(@RequestParam(value = "includeDeleted", defaultValue = "false") boolean includeDeleted) {
        return service.findAll(includeDeleted);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public ResponseEntity<Veicolo> findById(@PathVariable Long id) {
        Veicolo veicolo = service.findById(id);
        if (veicolo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(veicolo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public ResponseEntity<Veicolo> update(@PathVariable Long id, @RequestBody Veicolo entity) {
        if (service.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        service.restore(id);
        return ResponseEntity.noContent().build();
    }
}
