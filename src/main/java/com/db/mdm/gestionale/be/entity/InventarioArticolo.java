package com.db.mdm.gestionale.be.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventario_articolo", indexes = {
    @Index(name = "idx_inventario_magazzino", columnList = "magazzino_id"),
    @Index(name = "idx_inventario_categoria", columnList = "categoria"),
    @Index(name = "idx_inventario_nome", columnList = "nome")
})
public class InventarioArticolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "magazzino_id", nullable = false)
    private Magazzino magazzino;

    @Column(name = "categoria", nullable = false, length = 100)
    private String categoria;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "prezzo_unitario", precision = 12, scale = 2, nullable = false)
    private BigDecimal prezzoUnitario = BigDecimal.ZERO;

    @Column(name = "quantita_magazzino", precision = 12, scale = 3, nullable = false)
    private BigDecimal quantitaMagazzino = BigDecimal.ZERO;

    @Column(name = "valore_inventario", precision = 14, scale = 2,
            insertable = false, updatable = false)
    private BigDecimal valoreInventario;

    @Column(name = "livello_riordino", precision = 12, scale = 3, nullable = false)
    private BigDecimal livelloRiordino = BigDecimal.ZERO;

    @Column(name = "quantita_in_riordino", precision = 12, scale = 3, nullable = false)
    private BigDecimal quantitaInRiordino = BigDecimal.ZERO;

    @Column(name = "fuori_produzione", nullable = false)
    private boolean fuoriProduzione = false;
}
