package com.db.mdm.gestionale.be.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.db.mdm.gestionale.be.entity.Cliente;
import com.db.mdm.gestionale.be.service.ClienteService;

@RestController
@RequestMapping("/api/cliente")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public Cliente save(@RequestBody Cliente entity) {
        return service.save(entity);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public List<Cliente> findAll(@RequestParam(value = "includeDeleted", defaultValue = "false") boolean includeDeleted) {
        return service.findAll(includeDeleted);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public ResponseEntity<Cliente> findById(@PathVariable Long id) {
        Cliente cliente = service.findById(id);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cliente);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public ResponseEntity<Cliente> update(@PathVariable Long id, @RequestBody Cliente entity) {
        Cliente existing = service.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setNome(entity.getNome());
        existing.setRiferimento(entity.getRiferimento());
        existing.setTelefono(entity.getTelefono());
        existing.setEmail(entity.getEmail());
        existing.setIndirizzo(entity.getIndirizzo());
        return ResponseEntity.ok(service.save(existing));
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
