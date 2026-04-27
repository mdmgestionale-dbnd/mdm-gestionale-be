package com.db.mdm.gestionale.be.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.db.mdm.gestionale.be.repository.MagazzinoRepository;
import com.db.mdm.gestionale.be.service.MagazzinoService;
import com.db.mdm.gestionale.be.service.WebSocketService;
import com.db.mdm.gestionale.be.utils.Constants;
import com.db.mdm.gestionale.be.entity.Magazzino;

@Service
@RequiredArgsConstructor
public class MagazzinoServiceImpl implements MagazzinoService {
    private final MagazzinoRepository repository;
    private final WebSocketService webSocketService;

    @Override
    public Magazzino save(Magazzino entity) {
        boolean isNew = entity.getId() == null;
        Magazzino saved = repository.save(entity);
        String action = isNew ? "create" : "update";
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"magazzino\",\"action\":\"" + action + "\",\"id\":" + saved.getId() + "}");
        return saved;
    }

    @Override
    public List<Magazzino> findAll() {
        return repository.findAll();
    }

    @Override
    public Magazzino findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"magazzino\",\"action\":\"delete\",\"id\":" + id + "}");
    }
}
