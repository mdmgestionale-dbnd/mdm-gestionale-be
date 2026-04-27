package com.db.mdm.gestionale.be.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventario_movimento", indexes = {
    @Index(name = "idx_movimento_inventario", columnList = "inventario_id"),
    @Index(name = "idx_movimento_assegnazione", columnList = "assegnazione_id"),
    @Index(name = "idx_movimento_data", columnList = "movimento_at")
})
public class InventarioMovimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inventario_id", nullable = false)
    private InventarioArticolo inventario;

    @ManyToOne
    @JoinColumn(name = "assegnazione_id")
    private Assegnazione assegnazione;

    @Column(name = "quantita", precision = 12, scale = 3, nullable = false)
    private BigDecimal quantita;

    @Column(name = "movimento_at", nullable = false)
    private LocalDateTime movimentoAt;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @PrePersist
    void prePersist() {
        if (movimentoAt == null) {
            movimentoAt = LocalDateTime.now();
        }
    }
}
