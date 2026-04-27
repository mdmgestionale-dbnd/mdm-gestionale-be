package com.db.mdm.gestionale.be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.db.mdm.gestionale.be.entity.Cantiere;

public interface CantiereRepository extends JpaRepository<Cantiere, Long> {
    List<Cantiere> findByIsDeletedFalse();
    List<Cantiere> findByClienteId(Long clienteId);
    List<Cantiere> findByClienteIdAndIsDeletedFalse(Long clienteId);
    long countByIsDeletedFalse();

    @Query(value = "select nextval('cantiere_codice_seq')", nativeQuery = true)
    long nextCodiceNumber();
}
