package com.db.mdm.gestionale.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.db.mdm.gestionale.be.entity.Magazzino;

public interface MagazzinoRepository extends JpaRepository<Magazzino, Long> {
}
