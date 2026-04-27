package com.db.mdm.gestionale.be.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.db.mdm.gestionale.be.entity.Assegnazione;

public interface AssegnazioneRepository extends JpaRepository<Assegnazione, Long> {
    List<Assegnazione> findByIsDeletedFalseOrderByStartAtAsc();
    List<Assegnazione> findByIsDeletedTrueOrderByStartAtAsc();
    List<Assegnazione> findByCantiereId(Long cantiereId);
    List<Assegnazione> findByCantiereClienteId(Long clienteId);
    @Query("""
        select a from Assegnazione a
        where a.isDeleted = false
          and a.startAt < :to
          and a.endAt > :from
        order by a.startAt asc
    """)
    List<Assegnazione> findOverlappingRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    @Query(value = """
        select distinct a.*
        from assegnazione_membro am
        join assegnazione a on a.id = am.assegnazione_id
        where a.is_deleted = false
          and am.utente_id = :utenteId
        order by a.start_at asc
    """, nativeQuery = true)
    List<Assegnazione> findDipendenteAssignments(@Param("utenteId") Long utenteId);

    @Query(value = """
        select distinct a.*
        from assegnazione_membro am
        join assegnazione a on a.id = am.assegnazione_id
        where a.is_deleted = false
          and am.utente_id = :utenteId
          and a.end_at > cast(:from as timestamp)
          and a.start_at < cast(:to as timestamp)
        order by a.start_at asc
    """, nativeQuery = true)
    List<Assegnazione> findDipendenteAssignmentsBetween(
            @Param("utenteId") Long utenteId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
    List<Assegnazione> findTop10ByIsDeletedFalseAndEndAtAfterOrderByStartAtAsc(LocalDateTime now);
    long countByIsDeletedFalse();
    long countByIsDeletedFalseAndStartAtBetween(LocalDateTime from, LocalDateTime to);

    @Query("""
        select count(a) > 0
        from Assegnazione a
        where a.isDeleted = false
          and exists (
            select 1 from AssegnazioneMembro am
            where am.assegnazione.id = a.id
              and am.utente.id = :utenteId
          )
          and (:excludeAssegnazioneId is null or a.id <> :excludeAssegnazioneId)
          and a.startAt < :newEnd
          and a.endAt > :newStart
    """)
    boolean existsUserConflict(
            @Param("utenteId") Long utenteId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd,
            @Param("excludeAssegnazioneId") Long excludeAssegnazioneId);

    @Query("""
        select count(a) > 0
        from Assegnazione a
        where a.isDeleted = false
          and exists (
            select 1 from AssegnazioneVeicolo av
            where av.assegnazione.id = a.id
              and av.veicolo.id = :veicoloId
          )
          and (:excludeAssegnazioneId is null or a.id <> :excludeAssegnazioneId)
          and a.startAt < :newEnd
          and a.endAt > :newStart
    """)
    boolean existsVehicleConflict(
            @Param("veicoloId") Long veicoloId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd,
            @Param("excludeAssegnazioneId") Long excludeAssegnazioneId);
}
