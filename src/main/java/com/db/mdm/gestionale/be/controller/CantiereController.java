package com.db.mdm.gestionale.be.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.db.mdm.gestionale.be.entity.Allegato;
import com.db.mdm.gestionale.be.entity.Cantiere;
import com.db.mdm.gestionale.be.service.CantiereService;
import com.db.mdm.gestionale.be.service.AllegatoService;
import com.db.mdm.gestionale.be.service.WebSocketService;
import com.db.mdm.gestionale.be.utils.Constants;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cantiere")
@RequiredArgsConstructor
public class CantiereController {
    private final CantiereService service;
    private final AllegatoService allegatoService;
    private final WebSocketService webSocketService;

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> createCantiere(
            @RequestPart("cantiere") Cantiere cantiere,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            Cantiere saved = service.createCantiereWithOptionalFile(cantiere, file);
            webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED, "{\"entity\":\"cantiere\",\"action\":\"create\",\"id\":" + saved.getId() + "}");
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    @PutMapping(path = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> updateCantiere(
            @PathVariable Long id,
            @RequestPart("cantiere") Cantiere cantiere,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "removeFile", required = false, defaultValue = "false") boolean removeFile) {
        try {
            Cantiere updated = service.updateCantiereWithOptionalFile(id, cantiere, file, removeFile);
            webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED, "{\"entity\":\"cantiere\",\"action\":\"update\",\"id\":" + updated.getId() + "}");
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    @GetMapping
    public List<Cantiere> findAll(@RequestParam(value = "includeDeleted", defaultValue = "false") boolean includeDeleted) {
        return service.findAll(includeDeleted);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    @GetMapping("/{id}")
    public ResponseEntity<Cantiere> findById(@PathVariable Long id) {
        Optional<Cantiere> opt = service.findOptionalById(id);
        return ResponseEntity.of(opt);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        service.softDelete(id);
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED, "{\"entity\":\"cantiere\",\"action\":\"delete\",\"id\":" + id + "}");
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        service.restore(id);
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED, "{\"entity\":\"cantiere\",\"action\":\"restore\",\"id\":" + id + "}");
        return ResponseEntity.noContent().build();
    }

    // Lista allegati del cantiere (filter by date)
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    @GetMapping("/{id}/allegati")
    public ResponseEntity<List<Allegato>> listAllegati(
            @PathVariable Long id,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(service.listAllegatiForCantiere(id, from, to));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    @PostMapping(path = "/{id}/allegati", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Allegato> uploadAllegato(@PathVariable Long id, @RequestPart("file") MultipartFile file) throws Exception {
        Cantiere cantiere = service.findOptionalById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cantiere non trovato"));
        Allegato allegato = allegatoService.saveAllegato(file, cantiere, null);
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED, "{\"entity\":\"allegato\",\"action\":\"create\",\"id\":" + allegato.getId() + "}");
        return ResponseEntity.ok(allegato);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    @GetMapping("/{cantiereId}/allegato/{allegatoId}")
    public ResponseEntity<Resource> downloadAllegato(@PathVariable Long cantiereId, @PathVariable Long allegatoId) {
        try {
            Allegato a = service.findAllegatoByIdAndCantiere(allegatoId, cantiereId);
            byte[] data = service.downloadFileFromStorage(a.getStoragePath());
            ByteArrayResource resource = new ByteArrayResource(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + a.getNomeFile() + "\"")
                    .contentType(MediaType.parseMediaType(a.getTipoFile()))
                    .body(resource);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    @DeleteMapping("/{cantiereId}/allegato/{allegatoId}")
    public ResponseEntity<Void> deleteAllegato(@PathVariable Long cantiereId, @PathVariable Long allegatoId) {
        service.findAllegatoByIdAndCantiere(allegatoId, cantiereId);
        allegatoService.softDelete(allegatoId);
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED, "{\"entity\":\"allegato\",\"action\":\"delete\",\"id\":" + allegatoId + "}");
        return ResponseEntity.noContent().build();
    }
}
