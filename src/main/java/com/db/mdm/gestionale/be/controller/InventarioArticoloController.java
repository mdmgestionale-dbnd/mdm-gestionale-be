package com.db.mdm.gestionale.be.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.db.mdm.gestionale.be.entity.InventarioArticolo;
import com.db.mdm.gestionale.be.service.InventarioArticoloService;

@RestController
@RequestMapping("/api/inventarioarticolo")
@RequiredArgsConstructor
public class InventarioArticoloController {
    private final InventarioArticoloService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public InventarioArticolo save(@RequestBody InventarioArticolo entity) {
        return service.save(entity);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public List<InventarioArticolo> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public ResponseEntity<InventarioArticolo> findById(@PathVariable Long id) {
        InventarioArticolo item = service.findById(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public ResponseEntity<InventarioArticolo> update(@PathVariable Long id, @RequestBody InventarioArticolo entity) {
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
