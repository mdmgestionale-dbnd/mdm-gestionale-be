package com.db.mdm.gestionale.be.service;

import java.util.List;
import com.db.mdm.gestionale.be.entity.InventarioMovimento;

public interface InventarioMovimentoService {
    InventarioMovimento save(InventarioMovimento entity);
    List<InventarioMovimento> findAll();
    InventarioMovimento findById(Long id);
    void delete(Long id);
}
