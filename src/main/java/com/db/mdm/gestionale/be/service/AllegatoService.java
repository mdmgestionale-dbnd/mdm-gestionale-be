package com.db.mdm.gestionale.be.service;

import java.util.List;
import java.util.Optional;

import com.db.mdm.gestionale.be.entity.Allegato;

public interface AllegatoService {
    List<Allegato> findAll();
    Optional<Allegato> findById(Long id);
    Allegato save(Allegato allegato);
    Allegato update(Long id, Allegato allegato);
    void delete(Long id);
}
