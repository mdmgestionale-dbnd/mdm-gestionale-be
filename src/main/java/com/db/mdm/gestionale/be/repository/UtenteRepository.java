package com.db.mdm.gestionale.be.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.db.mdm.gestionale.be.entity.Utente;

public interface UtenteRepository extends JpaRepository<Utente, Long> {
    Optional<Utente> findByUsername(String username);
	List<Utente> findByIsDeletedTrue();
	@Query("""
		select u from Utente u
		where u.isDeleted = false
		  and u.username <> 'superadmin'
		  and u.livello in :livelli
		order by u.nome asc, u.cognome asc, u.username asc
	""")
	List<Utente> findByLivelloInAndIsDeletedFalse(@Param("livelli") List<Integer> livelli);
}
