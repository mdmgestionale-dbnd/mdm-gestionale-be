package com.db.mdm.gestionale.be.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.mdm.gestionale.be.dto.PermessoDecisionDto;
import com.db.mdm.gestionale.be.dto.PermessoRequestDto;
import com.db.mdm.gestionale.be.entity.Permesso;
import com.db.mdm.gestionale.be.service.PermessoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/permesso")
@RequiredArgsConstructor
public class PermessoController {
    private final PermessoService service;

    @PostMapping("/request")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public Permesso request(@Valid @RequestBody PermessoRequestDto request) {
        return service.createRequest(request);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public List<Permesso> findMine() {
        return service.findMine();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public List<Permesso> findPending() {
        return service.findPending();
    }

    @PutMapping("/{id}/decision")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public Permesso decide(@PathVariable Long id, @RequestBody PermessoDecisionDto decision) {
        return service.decide(id, decision);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public Permesso save(@RequestBody Permesso entity) {
        return service.save(entity);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public List<Permesso> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public ResponseEntity<Permesso> findById(@PathVariable Long id) {
        Permesso item = service.findById(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
