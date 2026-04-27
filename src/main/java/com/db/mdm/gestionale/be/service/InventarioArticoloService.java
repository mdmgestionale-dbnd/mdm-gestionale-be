package com.db.mdm.gestionale.be.service;

import java.util.List;
import com.db.mdm.gestionale.be.entity.InventarioArticolo;

public interface InventarioArticoloService {
    InventarioArticolo save(InventarioArticolo entity);
    List<InventarioArticolo> findAll();
    InventarioArticolo findById(Long id);
    void delete(Long id);
}
