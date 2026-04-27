package com.db.mdm.gestionale.be.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.db.mdm.gestionale.be.repository.AssegnazioneVeicoloRepository;
import com.db.mdm.gestionale.be.service.AssegnazioneVeicoloService;
import com.db.mdm.gestionale.be.service.WebSocketService;
import com.db.mdm.gestionale.be.utils.Constants;
import com.db.mdm.gestionale.be.entity.AssegnazioneVeicolo;

@Service
@RequiredArgsConstructor
public class AssegnazioneVeicoloServiceImpl implements AssegnazioneVeicoloService {
    private final AssegnazioneVeicoloRepository repository;
    private final WebSocketService webSocketService;

    @Override
    public AssegnazioneVeicolo save(AssegnazioneVeicolo entity) {
        AssegnazioneVeicolo saved = repository.save(entity);
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"assegnazione-veicolo\",\"action\":\"upsert\",\"id\":" + saved.getId() + "}");
        return saved;
    }

    @Override
    public List<AssegnazioneVeicolo> findAll() {
        return repository.findAll();
    }

    @Override
    public AssegnazioneVeicolo findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"assegnazione-veicolo\",\"action\":\"delete\",\"id\":" + id + "}");
    }
}
