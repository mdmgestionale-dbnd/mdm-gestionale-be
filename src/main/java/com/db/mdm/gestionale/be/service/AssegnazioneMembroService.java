package com.db.mdm.gestionale.be.service;

import java.util.List;
import com.db.mdm.gestionale.be.entity.AssegnazioneMembro;

public interface AssegnazioneMembroService {
    AssegnazioneMembro save(AssegnazioneMembro entity);
    List<AssegnazioneMembro> findAll();
    AssegnazioneMembro findById(Long id);
    void delete(Long id);
}
