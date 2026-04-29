package com.db.mdm.gestionale.be.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifica", indexes = {
    @Index(name = "idx_notifica_tipo", columnList = "tipo"),
    @Index(name = "idx_notifica_letta", columnList = "letta"),
    @Index(name = "idx_notifica_scadenza", columnList = "data_scadenza"),
    @Index(name = "idx_notifica_ref", columnList = "riferimento_tipo, riferimento_id"),
    @Index(name = "idx_notifica_destinatario", columnList = "destinatario_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notifica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "titolo", nullable = false, length = 200)
    private String titolo;

    @Column(name = "messaggio", nullable = false, columnDefinition = "TEXT")
    private String messaggio;

    @Column(name = "livello", nullable = false, length = 20)
    private String livello = "INFO";

    @Column(name = "riferimento_tipo", length = 50)
    private String riferimentoTipo;

    @Column(name = "riferimento_id")
    private Long riferimentoId;

    @Column(name = "destinatario_id")
    private Long destinatarioId;

    @Column(name = "data_scadenza")
    private LocalDate dataScadenza;

    @Column(name = "chiave_unica", nullable = false, unique = true, length = 200)
    private String chiaveUnica;

    @Column(name = "letta", nullable = false)
    private boolean letta = false;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @jakarta.persistence.PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @jakarta.persistence.PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
