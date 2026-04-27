package com.db.mdm.gestionale.be.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permesso", indexes = {
    @Index(name = "idx_permesso_utente", columnList = "utente_id"),
    @Index(name = "idx_permesso_stato", columnList = "stato")
})
public class Permesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "stato", nullable = false, length = 20)
    private String stato = "IN_ATTESA";

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @ManyToOne
    @JoinColumn(name = "allegato_id")
    private Allegato allegato;

    @ManyToOne
    @JoinColumn(name = "richiesto_da")
    private Utente richiestoDa;

    @ManyToOne
    @JoinColumn(name = "approvato_da")
    private Utente approvatoDa;

    @Column(name = "approvato_at")
    private LocalDateTime approvatoAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (stato == null || stato.isBlank()) stato = "IN_ATTESA";
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
