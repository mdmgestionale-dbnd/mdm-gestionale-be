package com.db.mdm.gestionale.be.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.db.mdm.gestionale.be.dto.AssegnazioneCalendarioDto;
import com.db.mdm.gestionale.be.dto.AssegnazionePianificazioneRequestDto;
import com.db.mdm.gestionale.be.dto.DisponibilitaRequestDto;
import com.db.mdm.gestionale.be.dto.DisponibilitaResponseDto;
import com.db.mdm.gestionale.be.entity.Allegato;
import com.db.mdm.gestionale.be.entity.Assegnazione;
import com.db.mdm.gestionale.be.entity.AssegnazioneMembro;
import com.db.mdm.gestionale.be.entity.AssegnazioneVeicolo;
import com.db.mdm.gestionale.be.entity.Cantiere;
import com.db.mdm.gestionale.be.entity.InventarioArticolo;
import com.db.mdm.gestionale.be.entity.InventarioMovimento;
import com.db.mdm.gestionale.be.entity.Utente;
import com.db.mdm.gestionale.be.entity.Veicolo;
import com.db.mdm.gestionale.be.repository.AssegnazioneMembroRepository;
import com.db.mdm.gestionale.be.repository.AssegnazioneRepository;
import com.db.mdm.gestionale.be.repository.AssegnazioneVeicoloRepository;
import com.db.mdm.gestionale.be.repository.CantiereRepository;
import com.db.mdm.gestionale.be.repository.InventarioArticoloRepository;
import com.db.mdm.gestionale.be.repository.InventarioMovimentoRepository;
import com.db.mdm.gestionale.be.repository.UtenteRepository;
import com.db.mdm.gestionale.be.repository.VeicoloRepository;
import com.db.mdm.gestionale.be.service.AssegnazioneService;
import com.db.mdm.gestionale.be.service.AllegatoService;
import com.db.mdm.gestionale.be.service.PermessoService;
import com.db.mdm.gestionale.be.service.UtenteService;
import com.db.mdm.gestionale.be.service.WebSocketService;
import com.db.mdm.gestionale.be.utils.Constants;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssegnazioneServiceImpl implements AssegnazioneService {
    private final AssegnazioneRepository assegnazioneRepository;
    private final CantiereRepository cantiereRepository;
    private final UtenteRepository utenteRepository;
    private final VeicoloRepository veicoloRepository;
    private final AssegnazioneMembroRepository assegnazioneMembroRepository;
    private final AssegnazioneVeicoloRepository assegnazioneVeicoloRepository;
    private final InventarioArticoloRepository inventarioArticoloRepository;
    private final InventarioMovimentoRepository inventarioMovimentoRepository;
    private final PermessoService permessoService;
    private final AllegatoService allegatoService;
    private final UtenteService utenteService;
    private final WebSocketService webSocketService;

    @Override
    @Transactional
    public Assegnazione createPianificazione(AssegnazionePianificazioneRequestDto request) {
        validateRequest(request);

        Cantiere cantiere = cantiereRepository.findById(request.getCantiereId())
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Cantiere non trovato"));

        checkConflicts(request.getMembroIds(), request.getVeicoloIds(), request.getStartAt(), request.getEndAt(), null);

        Assegnazione entity = new Assegnazione();
        entity.setCantiere(cantiere);
        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());
        entity.setNote(request.getNote());
        entity.setMaterialiNote(request.getMaterialiNote());
        entity.setDeleted(false);

        Assegnazione saved = assegnazioneRepository.save(entity);

        replaceMembers(saved, request.getMembroIds());
        replaceVehicles(saved, request.getVeicoloIds());

        broadcastEntityChange("assegnazione", "create", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Assegnazione updatePianificazione(Long id, AssegnazionePianificazioneRequestDto request) {
        validateRequest(request);

        Assegnazione existing = assegnazioneRepository.findById(id)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Assegnazione non trovata"));

        Cantiere cantiere = cantiereRepository.findById(request.getCantiereId())
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Cantiere non trovato"));

        checkConflicts(request.getMembroIds(), request.getVeicoloIds(), request.getStartAt(), request.getEndAt(), id);

        existing.setCantiere(cantiere);
        existing.setStartAt(request.getStartAt());
        existing.setEndAt(request.getEndAt());
        existing.setNote(request.getNote());
        existing.setMaterialiNote(request.getMaterialiNote());
        Assegnazione saved = assegnazioneRepository.save(existing);

        replaceMembers(saved, request.getMembroIds());
        replaceVehicles(saved, request.getVeicoloIds());

        broadcastEntityChange("assegnazione", "update", saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssegnazioneCalendarioDto> findCalendario(LocalDateTime from, LocalDateTime to) {
        Utente current = utenteService.getCurrentUtenteOrNull();
        List<Assegnazione> assegnazioni;
        if (current != null && Integer.valueOf(2).equals(current.getLivello())) {
            assegnazioni = (from == null || to == null)
                    ? assegnazioneRepository.findDipendenteAssignments(current.getId())
                    : assegnazioneRepository.findDipendenteAssignmentsBetween(current.getId(), from, to);
        } else if (from == null || to == null) {
            assegnazioni = assegnazioneRepository.findByIsDeletedFalseOrderByStartAtAsc();
        } else {
            assegnazioni = assegnazioneRepository.findOverlappingRange(from, to);
        }

        return toCalendarioDto(assegnazioni);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssegnazioneCalendarioDto> findDeleted() {
        return toCalendarioDto(assegnazioneRepository.findByIsDeletedTrueOrderByStartAtAsc());
    }

    private List<AssegnazioneCalendarioDto> toCalendarioDto(List<Assegnazione> assegnazioni) {
        List<AssegnazioneCalendarioDto> out = new ArrayList<>();
        for (Assegnazione a : assegnazioni) {
            List<Long> membroIds = assegnazioneMembroRepository.findByAssegnazioneId(a.getId()).stream()
                    .map(x -> x.getUtente().getId())
                    .toList();
            List<String> membroNomi = assegnazioneMembroRepository.findByAssegnazioneId(a.getId()).stream()
                    .map(x -> displayName(x.getUtente()))
                    .toList();
            List<Long> veicoloIds = assegnazioneVeicoloRepository.findByAssegnazioneId(a.getId()).stream()
                    .map(x -> x.getVeicolo().getId())
                    .toList();
            List<String> veicoloNomi = assegnazioneVeicoloRepository.findByAssegnazioneId(a.getId()).stream()
                    .map(x -> x.getVeicolo().getTarga() + (x.getVeicolo().getModello() != null ? " - " + x.getVeicolo().getModello() : ""))
                    .toList();
            List<AssegnazioneCalendarioDto.MaterialeDto> materiali = List.of();

            out.add(AssegnazioneCalendarioDto.builder()
                    .id(a.getId())
                    .cantiereId(a.getCantiere().getId())
                    .cantiereNome(a.getCantiere().getNome())
                    .startAt(a.getStartAt())
                    .endAt(a.getEndAt())
                    .note(a.getNote())
                    .materialiNote(a.getMaterialiNote())
                    .membroIds(membroIds)
                    .membroNomi(membroNomi)
                    .veicoloIds(veicoloIds)
                    .veicoloNomi(veicoloNomi)
                    .materiali(materiali)
                    .build());
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilitaResponseDto checkDisponibilita(DisponibilitaRequestDto request) {
        if (request.getStartAt() == null || request.getEndAt() == null) {
            throw new IllegalArgumentException("Intervallo temporale obbligatorio");
        }
        if (!request.getEndAt().isAfter(request.getStartAt())) {
            throw new IllegalArgumentException("La data di fine deve essere successiva alla data di inizio");
        }

        List<Long> membriOccupati = new ArrayList<>();
        for (Long utenteId : nullSafeDistinct(request.getMembroIds())) {
            boolean userConflict = assegnazioneRepository.existsUserConflict(
                    utenteId, request.getStartAt(), request.getEndAt(), request.getExcludeAssegnazioneId());
            boolean leaveConflict = permessoService.hasApprovedLeaveOverlap(
                    utenteId,
                    request.getStartAt().toLocalDate(),
                    request.getEndAt().minusNanos(1).toLocalDate());
            if (userConflict || leaveConflict) {
                membriOccupati.add(utenteId);
            }
        }

        List<Long> veicoliOccupati = new ArrayList<>();
        for (Long veicoloId : nullSafeDistinct(request.getVeicoloIds())) {
            if (assegnazioneRepository.existsVehicleConflict(
                    veicoloId, request.getStartAt(), request.getEndAt(), request.getExcludeAssegnazioneId())) {
                veicoliOccupati.add(veicoloId);
            }
        }

        return DisponibilitaResponseDto.builder()
                .disponibili(membriOccupati.isEmpty() && veicoliOccupati.isEmpty())
                .membriOccupati(membriOccupati)
                .veicoliOccupati(veicoliOccupati)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Assegnazione findById(Long id) {
        return assegnazioneRepository.findById(id).filter(a -> !a.isDeleted()).orElse(null);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Assegnazione existing = assegnazioneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assegnazione non trovata"));
        inventarioMovimentoRepository.deleteByAssegnazioneId(id);
        existing.setDeleted(true);
        assegnazioneRepository.save(existing);
        broadcastEntityChange("assegnazione", "delete", id);
    }

    @Override
    @Transactional
    public void restore(Long id) {
        Assegnazione existing = assegnazioneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assegnazione non trovata"));
        if (existing.getCantiere() == null || existing.getCantiere().isDeleted()
                || (existing.getCantiere().getCliente() != null && existing.getCantiere().getCliente().isDeleted())) {
            throw new IllegalStateException("Ripristina prima cliente e cantiere associati");
        }
        existing.setDeleted(false);
        assegnazioneRepository.save(existing);
        broadcastEntityChange("assegnazione", "restore", id);
    }

    @Override
    @Transactional
    public Allegato uploadAllegato(Long id, MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File obbligatorio");
        }

        Assegnazione assegnazione = assegnazioneRepository.findById(id)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Assegnazione non trovata"));
        Utente current = utenteService.getCurrentUtenteOrNull();
        if (current == null) {
            throw new IllegalStateException("Utente corrente non disponibile");
        }
        if (Integer.valueOf(2).equals(current.getLivello())
                && !assegnazioneMembroRepository.existsByAssegnazioneIdAndUtenteId(id, current.getId())) {
            throw new IllegalStateException("Non puoi caricare allegati su un'assegnazione non tua");
        }

        Allegato allegato = allegatoService.saveAllegato(file, assegnazione.getCantiere(), current);
        broadcastEntityChange("allegato", "create", allegato.getId());
        return allegato;
    }

    @Override
    @Transactional
    public void addMateriale(Long id, AssegnazionePianificazioneRequestDto.MaterialeUsatoDto materiale) {
        Assegnazione assegnazione = assegnazioneRepository.findById(id)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Assegnazione non trovata"));
        Utente current = utenteService.getCurrentUtenteOrNull();
        if (current == null) {
            throw new IllegalStateException("Utente corrente non disponibile");
        }
        if (Integer.valueOf(2).equals(current.getLivello())
                && !assegnazioneMembroRepository.existsByAssegnazioneIdAndUtenteId(id, current.getId())) {
            throw new IllegalStateException("Non puoi registrare materiali su un'assegnazione non tua");
        }
        registerMaterials(assegnazione, List.of(materiale));
        broadcastEntityChange("inventario-movimento", "create", id);
    }

    @Override
    @Transactional
    public void updateMaterialiNote(Long id, String materialiNote) {
        Assegnazione assegnazione = assegnazioneRepository.findById(id)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Assegnazione non trovata"));
        Utente current = utenteService.getCurrentUtenteOrNull();
        if (current == null) {
            throw new IllegalStateException("Utente corrente non disponibile");
        }
        if (Integer.valueOf(2).equals(current.getLivello())
                && !assegnazioneMembroRepository.existsByAssegnazioneIdAndUtenteId(id, current.getId())) {
            throw new IllegalStateException("Non puoi modificare i materiali di un'assegnazione non tua");
        }
        assegnazione.setMaterialiNote(materialiNote == null ? "" : materialiNote.trim());
        assegnazioneRepository.save(assegnazione);
        broadcastEntityChange("assegnazione", "update-materiali", id);
    }

    private void validateRequest(AssegnazionePianificazioneRequestDto request) {
        if (request.getStartAt() == null || request.getEndAt() == null) {
            throw new IllegalArgumentException("Intervallo temporale obbligatorio");
        }
        if (!request.getEndAt().isAfter(request.getStartAt())) {
            throw new IllegalArgumentException("La data di fine deve essere successiva alla data di inizio");
        }
    }

    private void checkConflicts(
            List<Long> membroIds,
            List<Long> veicoloIds,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long excludeAssegnazioneId) {

        List<Long> cleanMembri = nullSafeDistinct(membroIds);
        for (Long utenteId : cleanMembri) {
            if (assegnazioneRepository.existsUserConflict(utenteId, startAt, endAt, excludeAssegnazioneId)) {
                throw new IllegalStateException("Utente " + utenteId + " già assegnato in questo intervallo");
            }
            if (permessoService.hasApprovedLeaveOverlap(utenteId, startAt.toLocalDate(), endAt.minusNanos(1).toLocalDate())) {
                throw new IllegalStateException("Utente " + utenteId + " assente per permesso/ferie/malattia nel periodo selezionato");
            }
        }

        List<Long> cleanVeicoli = nullSafeDistinct(veicoloIds);
        for (Long veicoloId : cleanVeicoli) {
            if (assegnazioneRepository.existsVehicleConflict(veicoloId, startAt, endAt, excludeAssegnazioneId)) {
                throw new IllegalStateException("Veicolo " + veicoloId + " già assegnato in questo intervallo");
            }
        }
    }

    private void replaceMembers(Assegnazione assegnazione, List<Long> membroIds) {
        assegnazioneMembroRepository.deleteByAssegnazioneId(assegnazione.getId());
        assegnazioneMembroRepository.flush();
        for (Long utenteId : nullSafeDistinct(membroIds)) {
            Utente utente = utenteRepository.findById(utenteId)
                    .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()) && Boolean.TRUE.equals(u.getAttivo()))
                    .orElseThrow(() -> new IllegalArgumentException("Utente non valido: " + utenteId));
            AssegnazioneMembro member = new AssegnazioneMembro();
            member.setAssegnazione(assegnazione);
            member.setUtente(utente);
            member.setCreatedAt(LocalDateTime.now());
            assegnazioneMembroRepository.save(member);
        }
    }

    private void replaceVehicles(Assegnazione assegnazione, List<Long> veicoloIds) {
        assegnazioneVeicoloRepository.deleteByAssegnazioneId(assegnazione.getId());
        assegnazioneVeicoloRepository.flush();
        for (Long veicoloId : nullSafeDistinct(veicoloIds)) {
            Veicolo veicolo = veicoloRepository.findById(veicoloId)
                    .filter(v -> !v.isDeleted())
                    .orElseThrow(() -> new IllegalArgumentException("Veicolo non valido: " + veicoloId));
            AssegnazioneVeicolo item = new AssegnazioneVeicolo();
            item.setAssegnazione(assegnazione);
            item.setVeicolo(veicolo);
            item.setCreatedAt(LocalDateTime.now());
            assegnazioneVeicoloRepository.save(item);
        }
    }

    private void registerMaterials(Assegnazione assegnazione, List<AssegnazionePianificazioneRequestDto.MaterialeUsatoDto> materiali) {
        if (materiali == null || materiali.isEmpty()) {
            return;
        }

        for (AssegnazionePianificazioneRequestDto.MaterialeUsatoDto m : materiali) {
            if (m.getInventarioId() == null || m.getQuantita() == null) {
                continue;
            }
            if (m.getQuantita().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            InventarioArticolo articolo = inventarioArticoloRepository.findById(m.getInventarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Articolo inventario non trovato: " + m.getInventarioId()));

            InventarioMovimento mov = new InventarioMovimento();
            mov.setInventario(articolo);
            mov.setAssegnazione(assegnazione);
            mov.setQuantita(m.getQuantita().negate());
            mov.setMovimentoAt(LocalDateTime.now());
            mov.setDescrizione(
                    m.getDescrizione() != null && !m.getDescrizione().isBlank()
                            ? m.getDescrizione()
                            : "Scarico materiale per cantiere " + assegnazione.getCantiere().getNome());
            inventarioMovimentoRepository.save(mov);
        }
    }

    private List<Long> nullSafeDistinct(List<Long> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return values.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    private String displayName(Utente utente) {
        String fullName = ((utente.getNome() == null ? "" : utente.getNome()) + " "
                + (utente.getCognome() == null ? "" : utente.getCognome())).trim();
        return fullName.isBlank() ? utente.getUsername() : fullName;
    }

    private void broadcastEntityChange(String entity, String action, Long id) {
        String payload = "{\"entity\":\"" + entity + "\",\"action\":\"" + action + "\",\"id\":" + id + "}";
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED, payload);
    }
}
