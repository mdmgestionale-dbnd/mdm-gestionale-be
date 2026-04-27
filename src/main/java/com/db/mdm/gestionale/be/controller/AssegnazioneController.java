package com.db.mdm.gestionale.be.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.db.mdm.gestionale.be.dto.AssegnazioneCalendarioDto;
import com.db.mdm.gestionale.be.dto.AssegnazionePianificazioneRequestDto;
import com.db.mdm.gestionale.be.dto.DisponibilitaRequestDto;
import com.db.mdm.gestionale.be.dto.DisponibilitaResponseDto;
import com.db.mdm.gestionale.be.entity.Allegato;
import com.db.mdm.gestionale.be.entity.Assegnazione;
import com.db.mdm.gestionale.be.service.AssegnazioneService;

@RestController
@RequestMapping("/api/assegnazione")
@RequiredArgsConstructor
public class AssegnazioneController {
    private final AssegnazioneService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public Assegnazione save(@Valid @RequestBody AssegnazionePianificazioneRequestDto request) {
        return service.createPianificazione(request);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<AssegnazioneCalendarioDto> findAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return service.findCalendario(from, to);
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public List<AssegnazioneCalendarioDto> findDeleted() {
        return service.findDeleted();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public ResponseEntity<Assegnazione> findById(@PathVariable Long id) {
        Assegnazione item = service.findById(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @PostMapping("/availability")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public DisponibilitaResponseDto checkAvailability(@RequestBody DisponibilitaRequestDto request) {
        return service.checkDisponibilita(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public Assegnazione update(@PathVariable Long id, @Valid @RequestBody AssegnazionePianificazioneRequestDto request) {
        return service.updatePianificazione(id, request);
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

    @PostMapping(path = "/{id}/allegati", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public Allegato uploadAllegato(@PathVariable Long id, @RequestPart("file") MultipartFile file) throws Exception {
        return service.uploadAllegato(id, file);
    }

    @PostMapping("/{id}/materiali")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addMateriale(
            @PathVariable Long id,
            @RequestBody AssegnazionePianificazioneRequestDto.MaterialeUsatoDto materiale) {
        service.addMateriale(id, materiale);
        return ResponseEntity.noContent().build();
    }
}
