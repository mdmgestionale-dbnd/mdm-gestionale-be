package com.db.mdm.gestionale.be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.db.mdm.gestionale.be.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByIsDeletedFalseOrderByNomeAsc();
    List<Cliente> findByIsDeletedTrueOrderByNomeAsc();
    List<Cliente> findAllByOrderByNomeAsc();
    long countByIsDeletedFalse();
}
