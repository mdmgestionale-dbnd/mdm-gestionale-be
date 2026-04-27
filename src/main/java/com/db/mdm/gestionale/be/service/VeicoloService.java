package com.db.mdm.gestionale.be.service;

import java.util.List;
import com.db.mdm.gestionale.be.entity.Veicolo;

public interface VeicoloService {
    Veicolo save(Veicolo entity);
    List<Veicolo> findAll(boolean includeDeleted);
    Veicolo findById(Long id);
    void softDelete(Long id);
    void restore(Long id);
}
