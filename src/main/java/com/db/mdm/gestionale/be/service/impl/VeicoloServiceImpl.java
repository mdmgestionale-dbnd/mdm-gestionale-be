package com.db.mdm.gestionale.be.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.db.mdm.gestionale.be.entity.Assegnazione;
import com.db.mdm.gestionale.be.repository.VeicoloRepository;
import com.db.mdm.gestionale.be.repository.AssegnazioneRepository;
import com.db.mdm.gestionale.be.repository.AssegnazioneVeicoloRepository;
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
    private final WebSocketService webSocketService;

    @Override
    public Veicolo save(Veicolo entity) {
        boolean isNew = entity.getId() == null;
        Veicolo saved = repository.save(entity);
        String action = isNew ? "create" : "update";
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"veicolo\",\"action\":\"" + action + "\",\"id\":" + saved.getId() + "}");
        return saved;
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
