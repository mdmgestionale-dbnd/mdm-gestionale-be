package com.db.mdm.gestionale.be.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.db.mdm.gestionale.be.repository.InventarioMovimentoRepository;
import com.db.mdm.gestionale.be.service.InventarioMovimentoService;
import com.db.mdm.gestionale.be.service.WebSocketService;
import com.db.mdm.gestionale.be.utils.Constants;
import com.db.mdm.gestionale.be.entity.InventarioMovimento;

@Service
@RequiredArgsConstructor
public class InventarioMovimentoServiceImpl implements InventarioMovimentoService {
    private final InventarioMovimentoRepository repository;
    private final WebSocketService webSocketService;

    @Override
    public InventarioMovimento save(InventarioMovimento entity) {
        boolean isNew = entity.getId() == null;
        InventarioMovimento saved = repository.save(entity);
        String action = isNew ? "create" : "update";
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"inventario-movimento\",\"action\":\"" + action + "\",\"id\":" + saved.getId() + "}");
        return saved;
    }

    @Override
    public List<InventarioMovimento> findAll() {
        return repository.findAll();
    }

    @Override
    public InventarioMovimento findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"inventario-movimento\",\"action\":\"delete\",\"id\":" + id + "}");
    }
}
