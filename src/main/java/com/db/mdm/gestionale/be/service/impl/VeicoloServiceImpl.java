package com.db.mdm.gestionale.be.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.db.mdm.gestionale.be.entity.Assegnazione;
import com.db.mdm.gestionale.be.entity.Notifica;
import com.db.mdm.gestionale.be.repository.VeicoloRepository;
import com.db.mdm.gestionale.be.repository.AssegnazioneRepository;
import com.db.mdm.gestionale.be.repository.AssegnazioneVeicoloRepository;
import com.db.mdm.gestionale.be.repository.NotificaRepository;
import com.db.mdm.gestionale.be.service.VeicoloService;
import com.db.mdm.gestionale.be.service.WebSocketService;
import com.db.mdm.gestionale.be.utils.Constants;
import com.db.mdm.gestionale.be.entity.Veicolo;

@Service
@RequiredArgsConstructor
public class VeicoloServiceImpl implements VeicoloService {
    private final VeicoloRepository repository;
    private final AssegnazioneRepository assegnazioneRepository;
    private final AssegnazioneVeicoloRepository assegnazioneVeicoloRepository;
    private final NotificaRepository notificaRepository;
    private final WebSocketService webSocketService;

    @Override
    public Veicolo save(Veicolo entity) {
        boolean isNew = entity.getId() == null;
        Veicolo before = !isNew ? repository.findById(entity.getId()).orElse(null) : null;
        LocalDate oldAssicurazione = before != null ? before.getScadenzaAssicurazione() : null;
        LocalDate oldRevisione = before != null ? before.getScadenzaRevisione() : null;
        LocalDate oldBollo = before != null ? before.getScadenzaBollo() : null;
        Veicolo saved = repository.save(entity);
        if (before != null) {
            List<String> changedDeadlineTypes = changedDeadlineTypes(oldAssicurazione, oldRevisione, oldBollo, saved);
            if (!changedDeadlineTypes.isEmpty()) {
                closeVehicleDeadlineNotifications(saved.getId(), changedDeadlineTypes);
            }
        }
        String action = isNew ? "create" : "update";
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"veicolo\",\"action\":\"" + action + "\",\"id\":" + saved.getId() + "}");
        return saved;
    }

    private List<String> changedDeadlineTypes(LocalDate oldAssicurazione, LocalDate oldRevisione, LocalDate oldBollo, Veicolo after) {
        List<String> changed = new ArrayList<>();
        if (!java.util.Objects.equals(oldAssicurazione, after.getScadenzaAssicurazione())) {
            changed.add("SCADENZA_ASSICURAZIONE");
        }
        if (!java.util.Objects.equals(oldRevisione, after.getScadenzaRevisione())) {
            changed.add("SCADENZA_REVISIONE");
        }
        if (!java.util.Objects.equals(oldBollo, after.getScadenzaBollo())) {
            changed.add("SCADENZA_BOLLO");
        }
        return changed;
    }

    private void closeVehicleDeadlineNotifications(Long veicoloId, List<String> changedTypes) {
        List<Notifica> notifiche = notificaRepository.findByRiferimentoTipoAndRiferimentoIdAndIsDeletedFalse("VEICOLO", veicoloId);
        notifiche = notifiche.stream().filter(n -> changedTypes.contains(n.getTipo())).toList();
        if (notifiche.isEmpty()) {
            return;
        }
        notifiche.forEach(n -> {
            n.setLetta(true);
            n.setDeleted(true);
        });
        notificaRepository.saveAll(notifiche);
        webSocketService.broadcast(Constants.MSG_NOTIFICATION, "{\"entity\":\"notifica\",\"action\":\"vehicle-renewed\",\"id\":" + veicoloId + "}");
    }

    @Override
    public List<Veicolo> findAll(boolean includeDeleted) {
        if (includeDeleted) {
            return repository.findAllByOrderByTargaAsc();
        }
        return repository.findByIsDeletedFalseOrderByTargaAsc();
    }

    @Override
    public Veicolo findById(Long id) {
        return repository.findById(id).filter(v -> !v.isDeleted()).orElse(null);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        repository.findById(id).ifPresent(v -> {
            v.setDeleted(true);
            repository.save(v);
            assegnazioneVeicoloRepository.findByVeicoloId(id).forEach(av -> {
                Assegnazione assegnazione = av.getAssegnazione();
                assegnazione.setDeleted(true);
                assegnazioneRepository.save(assegnazione);
            });
            webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                    "{\"entity\":\"veicolo\",\"action\":\"delete\",\"id\":" + id + "}");
        });
    }

    @Override
    @Transactional
    public void restore(Long id) {
        repository.findById(id).ifPresent(v -> {
            v.setDeleted(false);
            repository.save(v);
            assegnazioneVeicoloRepository.findByVeicoloId(id).forEach(av -> {
                Assegnazione assegnazione = av.getAssegnazione();
                boolean cantiereRipristinabile = assegnazione.getCantiere() == null
                        || (!assegnazione.getCantiere().isDeleted()
                        && (assegnazione.getCantiere().getCliente() == null
                        || !assegnazione.getCantiere().getCliente().isDeleted()));
                if (cantiereRipristinabile) {
                    assegnazione.setDeleted(false);
                    assegnazioneRepository.save(assegnazione);
                }
            });
            webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                    "{\"entity\":\"veicolo\",\"action\":\"restore\",\"id\":" + id + "}");
        });
    }
}
