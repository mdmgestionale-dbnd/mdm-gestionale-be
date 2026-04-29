package com.db.mdm.gestionale.be.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.db.mdm.gestionale.be.entity.Notifica;

public interface NotificaRepository extends JpaRepository<Notifica, Long> {
    List<Notifica> findByIsDeletedFalseOrderByCreatedAtDesc();
    List<Notifica> findByIsDeletedFalseAndDestinatarioIdOrderByCreatedAtDesc(Long destinatarioId);
    List<Notifica> findByIsDeletedFalseAndLettaFalseOrderByCreatedAtDesc();
    List<Notifica> findByIsDeletedFalseAndLettaFalseAndDestinatarioIdOrderByCreatedAtDesc(Long destinatarioId);
    @Query("""
        select n from Notifica n
        where n.isDeleted = false
          and (
            n.letta = false
            or (n.dataScadenza is not null and n.dataScadenza <= :activeDeadlineLimit)
          )
        order by
          case n.livello when 'ERROR' then 0 when 'WARN' then 1 else 2 end,
          n.createdAt desc
    """)
    List<Notifica> findActionable(@Param("activeDeadlineLimit") LocalDate activeDeadlineLimit);
    @Query("""
        select n from Notifica n
        where n.isDeleted = false
          and n.destinatarioId = :destinatarioId
          and (
            n.letta = false
            or (n.dataScadenza is not null and n.dataScadenza <= :activeDeadlineLimit)
          )
        order by
          case n.livello when 'ERROR' then 0 when 'WARN' then 1 else 2 end,
          n.createdAt desc
    """)
    List<Notifica> findActionableForUser(
            @Param("destinatarioId") Long destinatarioId,
            @Param("activeDeadlineLimit") LocalDate activeDeadlineLimit);
    Optional<Notifica> findByChiaveUnicaAndIsDeletedFalse(String chiaveUnica);
    Optional<Notifica> findByChiaveUnica(String chiaveUnica);
    List<Notifica> findByRiferimentoTipoAndIsDeletedFalse(String riferimentoTipo);
    List<Notifica> findByRiferimentoTipoAndRiferimentoIdAndIsDeletedFalse(String riferimentoTipo, Long riferimentoId);
    List<Notifica> findByIsDeletedFalseAndDataScadenzaBefore(LocalDate date);
    long countByIsDeletedFalseAndLettaFalse();
}
