package com.db.mdm.gestionale.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.db.mdm.gestionale.be.entity.InventarioMovimento;

public interface InventarioMovimentoRepository extends JpaRepository<InventarioMovimento, Long> {
    List<InventarioMovimento> findByAssegnazioneIdOrderByMovimentoAtDesc(Long assegnazioneId);
    void deleteByAssegnazioneId(Long assegnazioneId);
}
