package com.db.mdm.gestionale.be.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.db.mdm.gestionale.be.repository.ClienteRepository;
import com.db.mdm.gestionale.be.repository.CantiereRepository;
import com.db.mdm.gestionale.be.repository.AssegnazioneRepository;
import com.db.mdm.gestionale.be.service.ClienteService;
import com.db.mdm.gestionale.be.service.WebSocketService;
import com.db.mdm.gestionale.be.utils.Constants;
import com.db.mdm.gestionale.be.entity.Assegnazione;
import com.db.mdm.gestionale.be.entity.Cantiere;
import com.db.mdm.gestionale.be.entity.Cliente;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository repository;
    private final CantiereRepository cantiereRepository;
    private final AssegnazioneRepository assegnazioneRepository;
    private final WebSocketService webSocketService;

    @Override
    public Cliente save(Cliente entity) {
        boolean isNew = entity.getId() == null;
        if (isNew) {
            entity.setDeleted(false);
        }
        Cliente saved = repository.save(entity);
        String action = isNew ? "create" : "update";
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"cliente\",\"action\":\"" + action + "\",\"id\":" + saved.getId() + "}");
        return saved;
    }

    @Override
    public List<Cliente> findAll(boolean includeDeleted) {
        if (includeDeleted) {
            return repository.findAllByOrderByNomeAsc();
        }
        return repository.findByIsDeletedFalseOrderByNomeAsc();
    }

    @Override
    public Cliente findById(Long id) {
        return repository.findById(id).filter(c -> !Boolean.TRUE.equals(c.isDeleted())).orElse(null);
    }

    @Override
    public void softDelete(Long id) {
        repository.findById(id).ifPresent(c -> {
            c.setDeleted(true);
            repository.save(c);
            for (Cantiere cantiere : cantiereRepository.findByClienteId(id)) {
                cantiere.setDeleted(true);
                cantiereRepository.save(cantiere);
                for (Assegnazione assegnazione : assegnazioneRepository.findByCantiereId(cantiere.getId())) {
                    assegnazione.setDeleted(true);
                    assegnazioneRepository.save(assegnazione);
                }
            }
            webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                    "{\"entity\":\"cliente\",\"action\":\"delete\",\"id\":" + id + "}");
        });
    }

    @Override
    public void restore(Long id) {
        repository.findById(id).ifPresent(c -> {
            c.setDeleted(false);
            repository.save(c);
            for (Cantiere cantiere : cantiereRepository.findByClienteId(id)) {
                cantiere.setDeleted(false);
                cantiereRepository.save(cantiere);
                for (Assegnazione assegnazione : assegnazioneRepository.findByCantiereId(cantiere.getId())) {
                    assegnazione.setDeleted(false);
                    assegnazioneRepository.save(assegnazione);
                }
            }
            webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                    "{\"entity\":\"cliente\",\"action\":\"restore\",\"id\":" + id + "}");
        });
    }
}
