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
@Table(name = "allegato_assegnazione")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllegatoAssegnazione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assegnazione_id", nullable = false)
    private Assegnazione assegnazione;

    @ManyToOne(optional = false)
    @JoinColumn(name = "allegato_id", nullable = false)
    private Allegato allegato;

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "is_documento_di_verifica")
    private Boolean isDocumentoDiVerifica = false;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime createdAt;
}
