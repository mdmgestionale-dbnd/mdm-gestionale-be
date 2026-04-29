package com.db.mdm.gestionale.be.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.db.mdm.gestionale.be.dto.PermessoDecisionDto;
import com.db.mdm.gestionale.be.dto.PermessoRequestDto;
import com.db.mdm.gestionale.be.entity.Allegato;
import com.db.mdm.gestionale.be.entity.AssegnazioneMembro;
import com.db.mdm.gestionale.be.entity.Permesso;
import com.db.mdm.gestionale.be.entity.Utente;
import com.db.mdm.gestionale.be.repository.AllegatoRepository;
import com.db.mdm.gestionale.be.repository.AssegnazioneMembroRepository;
import com.db.mdm.gestionale.be.repository.NotificaRepository;
import com.db.mdm.gestionale.be.repository.PermessoRepository;
import com.db.mdm.gestionale.be.repository.UtenteRepository;
import com.db.mdm.gestionale.be.entity.Notifica;
import com.db.mdm.gestionale.be.service.PermessoService;
import com.db.mdm.gestionale.be.service.WebSocketService;
import com.db.mdm.gestionale.be.utils.Constants;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermessoServiceImpl implements PermessoService {
    private static final String STATO_IN_ATTESA = "IN_ATTESA";
    private static final String STATO_APPROVATO = "APPROVATO";
    private static final String STATO_RIFIUTATO = "RIFIUTATO";

    private final PermessoRepository repository;
    private final UtenteRepository utenteRepository;
    private final AllegatoRepository allegatoRepository;
    private final AssegnazioneMembroRepository assegnazioneMembroRepository;
    private final NotificaRepository notificaRepository;
    private final WebSocketService webSocketService;

    @Override
    @Transactional
    public Permesso createRequest(PermessoRequestDto request) {
        validateRequest(request);

        Utente current = getCurrentUserOrThrow();
        Permesso entity = new Permesso();
        entity.setUtente(current);
        entity.setTipo(normalizeTipo(request.getTipo()));
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setNote(request.getNote());
        entity.setStato(STATO_IN_ATTESA);
        entity.setRichiestoDa(current);
        if (request.getAllegatoId() != null) {
            Allegato allegato = allegatoRepository.findById(request.getAllegatoId())
                    .orElseThrow(() -> new IllegalArgumentException("Allegato non trovato"));
            entity.setAllegato(allegato);
        }

        Permesso saved = repository.save(entity);
        createLeaveRequestNotification(saved);
        broadcastPermessoChange("create", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Permesso decide(Long id, PermessoDecisionDto decision) {
        Permesso permesso = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permesso non trovato"));

        if (!STATO_IN_ATTESA.equals(permesso.getStato())) {
            throw new IllegalStateException("Solo richieste in attesa possono essere elaborate");
        }

        Utente reviewer = getCurrentUserOrThrow();
        permesso.setApprovatoDa(reviewer);
        permesso.setApprovatoAt(LocalDateTime.now());
        permesso.setStato(decision.isApprova() ? STATO_APPROVATO : STATO_RIFIUTATO);

        if (decision.getNote() != null && !decision.getNote().isBlank()) {
            String existing = permesso.getNote() == null ? "" : permesso.getNote().trim();
            String suffix = "\n[Decisione] " + decision.getNote().trim();
            permesso.setNote(existing.isEmpty() ? decision.getNote().trim() : existing + suffix);
        }

        Permesso saved = repository.save(permesso);

        if (decision.isApprova()) {
            removeUserFromOverlappingAssignments(saved);
        }

        createLeaveDecisionNotification(saved, decision.isApprova());
        broadcastPermessoChange(decision.isApprova() ? "approve" : "reject", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Permesso save(Permesso entity) {
        if (entity.getStartDate() != null && entity.getEndDate() != null && entity.getEndDate().isBefore(entity.getStartDate())) {
            throw new IllegalArgumentException("Intervallo date non valido");
        }
        if (entity.getStato() == null || entity.getStato().isBlank()) {
            entity.setStato(STATO_IN_ATTESA);
        }
        boolean isNew = entity.getId() == null;
        Permesso saved = repository.save(entity);
        broadcastPermessoChange(isNew ? "create-admin" : "update-admin", saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permesso> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permesso> findMine() {
        Utente current = getCurrentUserOrThrow();
        return repository.findByUtenteIdOrderByCreatedAtDesc(current.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permesso> findPending() {
        return repository.findByStatoOrderByCreatedAtDesc(STATO_IN_ATTESA);
    }

    @Override
    @Transactional(readOnly = true)
    public Permesso findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
        broadcastPermessoChange("delete", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasApprovedLeaveOverlap(Long utenteId, LocalDate startDate, LocalDate endDate) {
        return repository.existsApprovedOverlap(utenteId, startDate, endDate);
    }

    private void validateRequest(PermessoRequestDto request) {
        if (request.getTipo() == null || request.getTipo().isBlank()) {
            throw new IllegalArgumentException("Tipo permesso obbligatorio");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Date permesso obbligatorie");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("La data di fine non puo essere prima della data di inizio");
        }
    }

    private String normalizeTipo(String tipoRaw) {
        String tipo = tipoRaw.trim().toUpperCase();
        if (!"FERIE".equals(tipo) && !"MALATTIA".equals(tipo)) {
            throw new IllegalArgumentException("Tipo non valido. Usa FERIE o MALATTIA");
        }
        return tipo;
    }

    private void removeUserFromOverlappingAssignments(Permesso permesso) {
        LocalDateTime startAt = permesso.getStartDate().atStartOfDay();
        LocalDateTime endAt = permesso.getEndDate().plusDays(1).atStartOfDay();

        List<AssegnazioneMembro> overlaps = assegnazioneMembroRepository.findOverlappingAssignmentsForUser(
                permesso.getUtente().getId(), startAt, endAt);

        if (overlaps.isEmpty()) {
            return;
        }

        List<Long> changedAssignmentIds = overlaps.stream()
                .map(am -> am.getAssegnazione().getId())
                .distinct()
                .toList();
        assegnazioneMembroRepository.deleteAll(overlaps);

        for (Long assignmentId : changedAssignmentIds) {
            webSocketService.broadcast(
                    Constants.MSG_ENTITY_CHANGED,
                    "{\"entity\":\"assegnazione\",\"action\":\"member-removed-by-leave\",\"id\":" + assignmentId + "}");
        }
    }

    private void createLeaveRequestNotification(Permesso permesso) {
        String key = "PERMESSO:RICHIESTA:" + permesso.getId();
        if (notificaRepository.findByChiaveUnicaAndIsDeletedFalse(key).isPresent()) {
            return;
        }
        Notifica notifica = new Notifica();
        notifica.setTipo("RICHIESTA_PERMESSO");
        notifica.setTitolo("Nuova richiesta " + permesso.getTipo().toLowerCase());
        notifica.setMessaggio(displayName(permesso.getUtente()) + " ha richiesto " + permesso.getTipo().toLowerCase()
                + " dal " + permesso.getStartDate() + " al " + permesso.getEndDate() + ".");
        notifica.setLivello("WARN");
        notifica.setRiferimentoTipo("PERMESSO");
        notifica.setRiferimentoId(permesso.getId());
        notifica.setDestinatarioId(null);
        notifica.setChiaveUnica(key);
        notificaRepository.save(notifica);
        webSocketService.broadcast(Constants.MSG_NOTIFICATION, "{\"entity\":\"notifica\",\"action\":\"new\",\"id\":null}");
    }

    private void createLeaveDecisionNotification(Permesso permesso, boolean approvata) {
        String key = "PERMESSO:DECISIONE:" + permesso.getId() + ":" + permesso.getStato();
        if (notificaRepository.findByChiaveUnicaAndIsDeletedFalse(key).isPresent()) {
            return;
        }
        Notifica notifica = new Notifica();
        notifica.setTipo("ESITO_PERMESSO");
        notifica.setTitolo("Richiesta " + (approvata ? "approvata" : "rifiutata"));
        notifica.setMessaggio("La richiesta " + permesso.getTipo().toLowerCase() + " di "
                + displayName(permesso.getUtente()) + " e stata " + (approvata ? "approvata." : "rifiutata."));
        notifica.setLivello(approvata ? "INFO" : "WARN");
        notifica.setRiferimentoTipo("PERMESSO");
        notifica.setRiferimentoId(permesso.getId());
        notifica.setDestinatarioId(permesso.getUtente().getId());
        notifica.setChiaveUnica(key);
        notificaRepository.save(notifica);
        webSocketService.broadcast(Constants.MSG_NOTIFICATION, "{\"entity\":\"notifica\",\"action\":\"new\",\"id\":null}");
    }

    private String displayName(Utente utente) {
        String fullName = ((utente.getNome() == null ? "" : utente.getNome()) + " "
                + (utente.getCognome() == null ? "" : utente.getCognome())).trim();
        return fullName.isBlank() ? utente.getUsername() : fullName;
    }

    private Utente getCurrentUserOrThrow() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return utenteRepository.findByUsername(ud.getUsername())
                    .orElseThrow(() -> new IllegalStateException("Utente corrente non trovato"));
        }
        throw new IllegalStateException("Utente non autenticato");
    }

    private void broadcastPermessoChange(String action, Long id) {
        webSocketService.broadcast(
                Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"permesso\",\"action\":\"" + action + "\",\"id\":" + id + "}");
    }
}
