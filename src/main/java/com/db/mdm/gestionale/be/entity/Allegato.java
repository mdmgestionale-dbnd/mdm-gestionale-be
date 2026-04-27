package com.db.mdm.gestionale.be.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "allegato", indexes = {
    @Index(name = "idx_allegato_tipo", columnList = "tipo_file"),
    @Index(name = "idx_allegato_created_at", columnList = "created_at"),
    @Index(name = "idx_allegato_created_by", columnList = "created_by"),
    @Index(name = "idx_allegato_cantiere", columnList = "cantiere_id")
})
public class Allegato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cantiere_id", nullable = false)
    private Cantiere cantiere;

    @Column(name = "nome_file", nullable = false, length = 255)
    private String nomeFile;

    @Column(name = "tipo_file", nullable = false, length = 50)
    private String tipoFile;

    @Column(name = "storage_path", nullable = false, columnDefinition = "TEXT")
    private String storagePath;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Utente createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
