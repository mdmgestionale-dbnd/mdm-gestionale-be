package com.db.mdm.gestionale.be.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "impostazioni")
public class Impostazioni {

    @Id
    @Column(name = "chiave", length = 50)
    private String chiave;

    @Column(name = "valore", nullable = false, length = 255)
    private String valore;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "min_value")
    private Integer minValue;

    @Column(name = "max_value")
    private Integer maxValue;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;
}

