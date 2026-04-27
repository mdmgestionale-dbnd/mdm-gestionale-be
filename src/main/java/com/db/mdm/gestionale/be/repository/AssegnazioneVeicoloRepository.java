package com.db.mdm.gestionale.be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.db.mdm.gestionale.be.entity.AssegnazioneVeicolo;

public interface AssegnazioneVeicoloRepository extends JpaRepository<AssegnazioneVeicolo, Long> {
    List<AssegnazioneVeicolo> findByAssegnazioneId(Long assegnazioneId);
    List<AssegnazioneVeicolo> findByVeicoloId(Long veicoloId);
    void deleteByAssegnazioneId(Long assegnazioneId);
}
