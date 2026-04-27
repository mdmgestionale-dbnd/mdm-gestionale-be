package com.db.mdm.gestionale.be.service;

import java.util.List;
import com.db.mdm.gestionale.be.entity.AssegnazioneVeicolo;

public interface AssegnazioneVeicoloService {
    AssegnazioneVeicolo save(AssegnazioneVeicolo entity);
    List<AssegnazioneVeicolo> findAll();
    AssegnazioneVeicolo findById(Long id);
    void delete(Long id);
}
