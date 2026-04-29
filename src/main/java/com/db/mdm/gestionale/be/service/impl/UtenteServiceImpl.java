package com.db.mdm.gestionale.be.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.db.mdm.gestionale.be.dto.CurrentUserDto;
import com.db.mdm.gestionale.be.entity.Utente;
import com.db.mdm.gestionale.be.repository.UtenteRepository;
import com.db.mdm.gestionale.be.service.UtenteService;
import com.db.mdm.gestionale.be.service.WebSocketService;
import com.db.mdm.gestionale.be.utils.Constants;

@Service
public class UtenteServiceImpl implements UtenteService {

    private final UtenteRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final WebSocketService wsService;

    public UtenteServiceImpl(UtenteRepository repository, PasswordEncoder passwordEncoder, WebSocketService wsService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.wsService = wsService;
    }

    @Override
    public Optional<Utente> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Utente> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    public List<Utente> findAll() {
        return repository.findAll();
    }

    @Override
    public Utente save(Utente utente) {
        boolean isNew = utente.getId() == null;
        // Gestione password
        if (utente.getPassword() != null && !utente.getPassword().isEmpty() && !isPasswordEncoded(utente.getPassword())) {
            utente.setPassword(passwordEncoder.encode(utente.getPassword()));
        } else if (utente.getId() != null && (utente.getPassword() == null || utente.getPassword().isEmpty())) {
            Utente existing = repository.findById(utente.getId()).orElseThrow();
            utente.setPassword(existing.getPassword());
        }

        if (utente.getIsDeleted() == null) {
            utente.setIsDeleted(false);
        }
        if (utente.getAttivo() == null) {
            utente.setAttivo(true);
        }

        LocalDateTime now = LocalDateTime.now();

        if (utente.getId() == null) {
            // Nuovo utente → setta createdAt
            utente.setCreatedAt(now);
        } else {
            // Modifica → conserva createdAt esistente
            Utente existing = repository.findById(utente.getId()).orElseThrow();
            utente.setCreatedAt(existing.getCreatedAt());
        }

        // Aggiorna sempre updatedAt
        utente.setUpdatedAt(now);

        Utente saved = repository.save(utente);
        String action = isNew ? "create" : "update";
        wsService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"utente\",\"action\":\"" + action + "\",\"id\":" + saved.getId() + "}");
        return saved;
    }

    @Override
    public void deleteById(Long id) {
        repository.findById(id).ifPresent(u -> {
            u.setIsDeleted(true);
            u.setAttivo(false); // opzionale, per sicurezza
            repository.save(u);
        });
        wsService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"utente\",\"action\":\"delete\",\"id\":" + id + "}");
    }

    @Override
    public void restore(Long id) {
        repository.findById(id).ifPresent(u -> {
            u.setIsDeleted(false);
            u.setAttivo(true);
            u.setUpdatedAt(LocalDateTime.now());
            repository.save(u);
        });
        wsService.broadcast(Constants.MSG_ENTITY_CHANGED,
                "{\"entity\":\"utente\",\"action\":\"restore\",\"id\":" + id + "}");
    }


    private boolean isPasswordEncoded(String password) {
        return password != null && password.startsWith("$2");
    }

    @Override
    public List<Utente> findDipendenti() {
        return repository.findByLivelloInAndIsDeletedFalse(List.of(0, 1, 2));
    }

	@Override
	public Utente getCurrentUtenteOrNull() {
		Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            var username = ud.getUsername();
            var maybe = findByUsername(username);
            if (maybe.isPresent()) {
                Utente u = maybe.get();
                return u;
            }
        }
        return null;
	}
    
}
