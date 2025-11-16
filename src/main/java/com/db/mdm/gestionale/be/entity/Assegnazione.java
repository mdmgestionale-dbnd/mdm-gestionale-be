package com.db.mdm.gestionale.be.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assegnazione")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assegnazione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cantiere_id")
    private Cantiere cantiere;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "start_at", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    private LocalDateTime endAt;

    @Column(columnDefinition = "TEXT")
    private String luogo;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    private String stato = "planned";

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Utente createdBy;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime updatedAt;
}
