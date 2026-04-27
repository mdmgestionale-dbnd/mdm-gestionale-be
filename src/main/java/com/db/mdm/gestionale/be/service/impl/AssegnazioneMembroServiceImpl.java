package com.db.mdm.gestionale.be.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.db.mdm.gestionale.be.repository.AssegnazioneMembroRepository;
import com.db.mdm.gestionale.be.service.AssegnazioneMembroService;
import com.db.mdm.gestionale.be.service.WebSocketService;
import com.db.mdm.gestionale.be.utils.Constants;
import com.db.mdm.gestionale.be.entity.AssegnazioneMembro;

@Service
@RequiredArgsConstructor
public class AssegnazioneMembroServiceImpl implements AssegnazioneMembroService {
    private final AssegnazioneMembroRepository repository;
    private final WebSocketService webSocketService;

    @Override
    public AssegnazioneMembro save(AssegnazioneMembro entity) {
        AssegnazioneMembro saved = repository.save(entity);
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"assegnazione-membro\",\"action\":\"upsert\",\"id\":" + saved.getId() + "}");
        return saved;
    }

    @Override
    public List<AssegnazioneMembro> findAll() {
        return repository.findAll();
    }

    @Override
    public AssegnazioneMembro findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"assegnazione-membro\",\"action\":\"delete\",\"id\":" + id + "}");
    }
}
