package com.db.mdm.gestionale.be.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.db.mdm.gestionale.be.entity.Veicolo;

public interface VeicoloRepository extends JpaRepository<Veicolo, Long> {
    List<Veicolo> findByIsDeletedFalseOrderByTargaAsc();
    List<Veicolo> findByIsDeletedTrueOrderByTargaAsc();
    List<Veicolo> findAllByOrderByTargaAsc();
    long countByIsDeletedFalse();

    @Query("""
        select v from Veicolo v
        where v.isDeleted = false and (
            (v.scadenzaAssicurazione is not null and v.scadenzaAssicurazione <= :limit)
            or (v.scadenzaRevisione is not null and v.scadenzaRevisione <= :limit)
            or (v.scadenzaBollo is not null and v.scadenzaBollo <= :limit)
        )
        order by v.targa asc
    """)
    List<Veicolo> findExpiringByLimit(@Param("limit") LocalDate limit);
}
