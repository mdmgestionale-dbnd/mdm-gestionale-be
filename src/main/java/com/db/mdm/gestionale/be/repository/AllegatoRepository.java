package com.db.mdm.gestionale.be.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.db.mdm.gestionale.be.entity.Allegato;

public interface AllegatoRepository extends JpaRepository<Allegato, Long> {
    List<Allegato> findByCantiereIdAndIsDeletedFalse(Long cantiereId);
    List<Allegato> findByCantiereIdAndCreatedAtBetweenAndIsDeletedFalse(Long cantiereId, LocalDateTime from, LocalDateTime to);
    Optional<Allegato> findByIdAndCantiereIdAndIsDeletedFalse(Long id, Long cantiereId);
}
