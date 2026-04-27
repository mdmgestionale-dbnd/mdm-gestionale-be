package com.db.mdm.gestionale.be.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.db.mdm.gestionale.be.repository.InventarioArticoloRepository;
import com.db.mdm.gestionale.be.service.InventarioArticoloService;
import com.db.mdm.gestionale.be.service.WebSocketService;
import com.db.mdm.gestionale.be.utils.Constants;
import com.db.mdm.gestionale.be.entity.InventarioArticolo;

@Service
@RequiredArgsConstructor
public class InventarioArticoloServiceImpl implements InventarioArticoloService {
    private final InventarioArticoloRepository repository;
    private final WebSocketService webSocketService;

    @Override
    public InventarioArticolo save(InventarioArticolo entity) {
        boolean isNew = entity.getId() == null;
        entity.setCategoria("GENERALE");
        entity.setDescrizione(null);
        entity.setLivelloRiordino(java.math.BigDecimal.ZERO);
        entity.setQuantitaInRiordino(java.math.BigDecimal.ZERO);
        entity.setFuoriProduzione(false);
        InventarioArticolo saved = repository.save(entity);
        String action = isNew ? "create" : "update";
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"inventario-articolo\",\"action\":\"" + action + "\",\"id\":" + saved.getId() + "}");
        return saved;
    }

    @Override
    public List<InventarioArticolo> findAll() {
        return repository.findAll();
    }

    @Override
    public InventarioArticolo findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
        webSocketService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"inventario-articolo\",\"action\":\"delete\",\"id\":" + id + "}");
    }
}
