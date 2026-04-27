package com.db.mdm.gestionale.be.service;

import java.util.List;

import com.db.mdm.gestionale.be.dto.PermessoDecisionDto;
import com.db.mdm.gestionale.be.dto.PermessoRequestDto;
import com.db.mdm.gestionale.be.entity.Permesso;

public interface PermessoService {
    Permesso createRequest(PermessoRequestDto request);
    Permesso decide(Long id, PermessoDecisionDto decision);
    Permesso save(Permesso entity);
    List<Permesso> findAll();
    List<Permesso> findMine();
    List<Permesso> findPending();
    Permesso findById(Long id);
    void delete(Long id);
    boolean hasApprovedLeaveOverlap(Long utenteId, java.time.LocalDate startDate, java.time.LocalDate endDate);
}
