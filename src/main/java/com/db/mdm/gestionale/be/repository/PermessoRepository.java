package com.db.mdm.gestionale.be.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.db.mdm.gestionale.be.entity.Permesso;

public interface PermessoRepository extends JpaRepository<Permesso, Long> {
    List<Permesso> findByUtenteIdOrderByCreatedAtDesc(Long utenteId);
    List<Permesso> findByStatoOrderByCreatedAtDesc(String stato);
    List<Permesso> findByUtenteIdAndStatoOrderByCreatedAtDesc(Long utenteId, String stato);
    List<Permesso> findByUtenteIdAndStatoAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(
            Long utenteId, String stato, LocalDate rangeEnd, LocalDate rangeStart);

    @Query("""
        select count(p) > 0
        from Permesso p
        where p.utente.id = :utenteId
          and p.stato = 'APPROVATO'
          and p.startDate <= :rangeEnd
          and p.endDate >= :rangeStart
    """)
    boolean existsApprovedOverlap(
            @Param("utenteId") Long utenteId,
            @Param("rangeStart") LocalDate rangeStart,
            @Param("rangeEnd") LocalDate rangeEnd);
}
