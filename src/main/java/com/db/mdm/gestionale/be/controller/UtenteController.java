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

import com.db.mdm.gestionale.be.entity.Utente;
import com.db.mdm.gestionale.be.service.UtenteService;

@RestController
@RequestMapping("/api/utenti")
public class UtenteController {

    private final UtenteService utenteService;

    public UtenteController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public List<Utente> getAll() {
        return utenteService.findAll();
    }
    
    @GetMapping("/dipendenti")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public List<Utente> getDipendenti() {
        return utenteService.findDipendenti();
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public ResponseEntity<Utente> getById(@PathVariable Long id) {
        return utenteService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Utente create(@RequestBody Utente utente) {
        return utenteService.save(utente);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Utente> update(@PathVariable Long id, @RequestBody Utente utente) {
        return utenteService.findById(id)
                .map(existing -> {
                    utente.setId(id);
                    return ResponseEntity.ok(utenteService.save(utente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        return utenteService.findById(id)
                .map(u -> {
                    utenteService.deleteById(id); // ora logica
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        utenteService.restore(id);
        return ResponseEntity.noContent().build();
    }

}
