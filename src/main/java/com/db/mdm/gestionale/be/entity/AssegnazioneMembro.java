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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "assegnazione_membro",
    uniqueConstraints = @UniqueConstraint(columnNames = {"assegnazione_id", "utente_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssegnazioneMembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assegnazione_id", nullable = false)
    private Assegnazione assegnazione;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
