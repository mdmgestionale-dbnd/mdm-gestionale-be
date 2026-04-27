package com.db.mdm.gestionale.be.service;

import java.util.List;
import com.db.mdm.gestionale.be.entity.Magazzino;

public interface MagazzinoService {
    Magazzino save(Magazzino entity);
    List<Magazzino> findAll();
    Magazzino findById(Long id);
    void delete(Long id);
}
