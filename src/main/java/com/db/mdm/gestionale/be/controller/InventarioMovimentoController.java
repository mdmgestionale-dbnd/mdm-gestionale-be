package com.db.mdm.gestionale.be.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.db.mdm.gestionale.be.entity.InventarioMovimento;
import com.db.mdm.gestionale.be.service.InventarioMovimentoService;

@RestController
@RequestMapping("/api/inventariomovimento")
@RequiredArgsConstructor
public class InventarioMovimentoController {
    private final InventarioMovimentoService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public InventarioMovimento save(@RequestBody InventarioMovimento entity) {
        return service.save(entity);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public List<InventarioMovimento> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public ResponseEntity<InventarioMovimento> findById(@PathVariable Long id) {
        InventarioMovimento item = service.findById(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public ResponseEntity<InventarioMovimento> update(@PathVariable Long id, @RequestBody InventarioMovimento entity) {
        if (service.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
