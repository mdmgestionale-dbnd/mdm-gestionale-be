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
@Table(name = "veicolo", indexes = {
    @Index(name = "idx_veicolo_targa", columnList = "targa")
})
public class Veicolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "targa", nullable = false, unique = true, length = 20)
    private String targa;

    @Column(name = "marca", length = 100)
    private String marca;

    @Column(name = "modello", length = 100)
    private String modello;

    @Column(name = "scadenza_assicurazione")
    private LocalDate scadenzaAssicurazione;

    @Column(name = "scadenza_revisione")
    private LocalDate scadenzaRevisione;

    @Column(name = "scadenza_bollo")
    private LocalDate scadenzaBollo;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

