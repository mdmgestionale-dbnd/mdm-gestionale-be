package com.db.mdm.gestionale.be.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.db.mdm.gestionale.be.entity.Allegato;
import com.db.mdm.gestionale.be.entity.Cantiere;
import com.db.mdm.gestionale.be.entity.Cliente;
import com.db.mdm.gestionale.be.entity.Utente;
import com.db.mdm.gestionale.be.repository.AllegatoRepository;
import com.db.mdm.gestionale.be.repository.AssegnazioneRepository;
import com.db.mdm.gestionale.be.repository.CantiereRepository;
import com.db.mdm.gestionale.be.repository.ClienteRepository;
import com.db.mdm.gestionale.be.service.AllegatoService;
import com.db.mdm.gestionale.be.service.CantiereService;
import com.db.mdm.gestionale.be.service.UtenteService;

@Service
@RequiredArgsConstructor
public class CantiereServiceImpl implements CantiereService {

    private final CantiereRepository cantiereRepo;
    private final ClienteRepository clienteRepo;
    private final AssegnazioneRepository assegnazioneRepository;
    private final AllegatoService allegatoService;
    private final AllegatoRepository allegatoRepo;
    private final UtenteService utenteService; // per recuperare uploader (da sessione)

    @Transactional
    @Override
    public Cantiere createCantiereWithOptionalFile(Cantiere cantiere, MultipartFile file) throws Exception {
        if (cantiere.getCliente() == null || cantiere.getCliente().getId() == null) {
            throw new IllegalArgumentException("Il cantiere deve avere un cliente associato");
        }
        Cliente cli = clienteRepo.findById(cantiere.getCliente().getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato"));
        cantiere.setCliente(cli);
        cantiere.setCodice("C-" + String.format("%05d", cantiereRepo.nextCodiceNumber()));
        cantiere.setDescrizione(null);
        cantiere.setIndirizzo(null);
        cantiere.setReferente(null);
        cantiere.setDeleted(false);
        cantiere.setCreatedAt(LocalDateTime.now());
        Cantiere saved = cantiereRepo.save(cantiere);

        if (file != null) {
            // utente corrente (se disponibile)
            Utente u = utenteService.getCurrentUtenteOrNull();
            Allegato a = allegatoService.saveAllegato(file, saved, u);
            // allegatoRepo.save(a); allegatoService già salva
        }
        return saved;
    }

    @Transactional
    @Override
    public Cantiere updateCantiereWithOptionalFile(Long id, Cantiere payload, MultipartFile file, boolean removeFile) throws Exception {
        Cantiere existing = cantiereRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Cantiere non trovato"));
        if (payload.getNome() != null) {
            existing.setNome(payload.getNome());
        }
        if (payload.getCliente() != null && payload.getCliente().getId() != null) {
            Cliente cli = clienteRepo.findById(payload.getCliente().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato"));
            existing.setCliente(cli);
        }
        existing.setDescrizione(null);
        existing.setIndirizzo(null);
        existing.setReferente(null);
        Cantiere saved = cantiereRepo.save(existing);

        if (removeFile) {
            // mark allegati deleted for that cantiere OR delete last attached - business logic: ripulire tutti o l'ultimo?
            // qui mark all allegati of this cantiere as deleted
            List<Allegato> list = allegatoRepo.findByCantiereIdAndIsDeletedFalse(saved.getId());
            for (Allegato a : list) {
                a.setDeleted(true);
                allegatoRepo.save(a);
                // optionally delete from storage: allegatoService.deleteFileFromStorage(a.getStoragePath());
            }
        }

        if (file != null) {
            Utente u = utenteService.getCurrentUtenteOrNull();
            allegatoService.saveAllegato(file, saved, u);
        }

        return saved;
    }

    @Override
    public List<Cantiere> findAll(boolean includeDeleted) {
        if (includeDeleted) return cantiereRepo.findAll();
        return cantiereRepo.findByIsDeletedFalse();
    }

    @Override
    public Optional<Cantiere> findOptionalById(Long id) {
        return cantiereRepo.findById(id);
    }

    @Override
    public void softDelete(Long id) {
        Cantiere c = cantiereRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Cantiere non trovato"));
        c.setDeleted(true);
        cantiereRepo.save(c);
        assegnazioneRepository.findByCantiereId(id).forEach(a -> {
            a.setDeleted(true);
            assegnazioneRepository.save(a);
        });
    }

    @Override
    public void restore(Long id) {
        Cantiere c = cantiereRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Cantiere non trovato"));
        if (c.getCliente() != null && c.getCliente().isDeleted()) {
            throw new IllegalStateException("Ripristina prima il cliente associato");
        }
        c.setDeleted(false);
        cantiereRepo.save(c);
        assegnazioneRepository.findByCantiereId(id).forEach(a -> {
            a.setDeleted(false);
            assegnazioneRepository.save(a);
        });
    }

    @Override
    public List<Allegato> listAllegatiForCantiere(Long cantiereId, LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) {
            return allegatoRepo.findByCantiereIdAndIsDeletedFalse(cantiereId);
        }
        return allegatoRepo.findByCantiereIdAndCreatedAtBetweenAndIsDeletedFalse(cantiereId, from == null ? LocalDateTime.MIN : from, to == null ? LocalDateTime.MAX : to);
    }

    @Override
    public Allegato findAllegatoByIdAndCantiere(Long allegatoId, Long cantiereId) {
        return allegatoRepo.findByIdAndCantiereIdAndIsDeletedFalse(allegatoId, cantiereId)
                .orElseThrow(() -> new IllegalArgumentException("Allegato non trovato per questo cantiere"));
    }

    @Override
    public byte[] downloadFileFromStorage(String storagePath) throws Exception {
        // delega al supabase service tramite allegatoService
        return allegatoService.downloadFile(storagePath);
    }
}
