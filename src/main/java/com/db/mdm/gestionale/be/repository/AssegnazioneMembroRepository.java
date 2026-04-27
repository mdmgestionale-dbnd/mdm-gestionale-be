package com.db.mdm.gestionale.be.repository;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.db.mdm.gestionale.be.entity.AssegnazioneMembro;

public interface AssegnazioneMembroRepository extends JpaRepository<AssegnazioneMembro, Long> {
    List<AssegnazioneMembro> findByAssegnazioneId(Long assegnazioneId);
    boolean existsByAssegnazioneIdAndUtenteId(Long assegnazioneId, Long utenteId);
    void deleteByAssegnazioneId(Long assegnazioneId);

    @Query("""
        select am from AssegnazioneMembro am
        where am.utente.id = :utenteId
          and am.assegnazione.isDeleted = false
          and am.assegnazione.startAt < :endAt
          and am.assegnazione.endAt > :startAt
    """)
    List<AssegnazioneMembro> findOverlappingAssignmentsForUser(
            @Param("utenteId") Long utenteId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    @Query("""
        select am from AssegnazioneMembro am
        where am.assegnazione.isDeleted = false
          and am.assegnazione.startAt < :endAt
          and am.assegnazione.endAt > :startAt
    """)
    List<AssegnazioneMembro> findAllOverlapping(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);
}
