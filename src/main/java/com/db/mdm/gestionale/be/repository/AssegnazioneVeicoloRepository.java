package com.db.mdm.gestionale.be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.db.mdm.gestionale.be.entity.AssegnazioneVeicolo;

public interface AssegnazioneVeicoloRepository extends JpaRepository<AssegnazioneVeicolo, Long> {
    List<AssegnazioneVeicolo> findByAssegnazioneId(Long assegnazioneId);
    List<AssegnazioneVeicolo> findByVeicoloId(Long veicoloId);
    void deleteByAssegnazioneId(Long assegnazioneId);

    @Query("""
            select av from AssegnazioneVeicolo av
            join fetch av.assegnazione a
            join fetch av.veicolo v
            where a.isDeleted = false
              and a.endAt > :from
              and a.startAt < :to
            order by v.targa asc, a.startAt asc
            """)
    List<AssegnazioneVeicolo> findAllOverlapping(@Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);
}
