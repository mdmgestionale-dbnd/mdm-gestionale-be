package com.db.mdm.gestionale.be.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.db.mdm.gestionale.be.dto.OreLavorateReportDto;
import com.db.mdm.gestionale.be.entity.AssegnazioneMembro;
import com.db.mdm.gestionale.be.entity.Permesso;
import com.db.mdm.gestionale.be.entity.Utente;
import com.db.mdm.gestionale.be.repository.AssegnazioneMembroRepository;
import com.db.mdm.gestionale.be.repository.PermessoRepository;
import com.db.mdm.gestionale.be.repository.UtenteRepository;
import com.db.mdm.gestionale.be.service.ReportService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final AssegnazioneMembroRepository assegnazioneMembroRepository;
    private final PermessoRepository permessoRepository;
    private final UtenteRepository utenteRepository;

    @Override
    @Transactional(readOnly = true)
    public OreLavorateReportDto getOreLavorate(LocalDate from, LocalDate to, Long utenteId) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Intervallo data obbligatorio");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("La data finale non puo essere precedente alla data iniziale");
        }

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime endExclusive = to.plusDays(1).atStartOfDay();

        Map<Long, OreAgg> map = new LinkedHashMap<>();
        for (AssegnazioneMembro am : assegnazioneMembroRepository.findAllOverlapping(start, endExclusive)) {
            Utente u = am.getUtente();
            if (utenteId != null && !utenteId.equals(u.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(u.getIsDeleted())) {
                continue;
            }

            LocalDateTime assignmentStart = am.getAssegnazione().getStartAt();
            LocalDateTime assignmentEnd = am.getAssegnazione().getEndAt();
            LocalDateTime effectiveStart = assignmentStart.isBefore(start) ? start : assignmentStart;
            LocalDateTime effectiveEnd = assignmentEnd.isAfter(endExclusive) ? endExclusive : assignmentEnd;
            if (!effectiveEnd.isAfter(effectiveStart)) {
                continue;
            }

            double hours = Duration.between(effectiveStart, effectiveEnd).toMinutes() / 60.0;

            OreAgg agg = map.computeIfAbsent(u.getId(), x -> new OreAgg());
            agg.utente = u;
            agg.ore += hours;
        }

        if (utenteId != null && !map.containsKey(utenteId)) {
            utenteRepository.findById(utenteId).ifPresent(u -> {
                if (!Boolean.TRUE.equals(u.getIsDeleted())) {
                    OreAgg agg = new OreAgg();
                    agg.utente = u;
                    map.put(utenteId, agg);
                }
            });
        }

        for (Map.Entry<Long, OreAgg> entry : map.entrySet()) {
            Long userId = entry.getKey();
            List<Permesso> approved = permessoRepository
                    .findByUtenteIdAndStatoAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(
                            userId, "APPROVATO", to, from);
            long assenze = 0;
            for (Permesso p : approved) {
                LocalDate d1 = p.getStartDate().isBefore(from) ? from : p.getStartDate();
                LocalDate d2 = p.getEndDate().isAfter(to) ? to : p.getEndDate();
                assenze += Duration.between(d1.atStartOfDay(), d2.plusDays(1).atStartOfDay()).toDays();
            }
            entry.getValue().giorniAssenza = assenze;
        }

        List<OreLavorateReportDto.Riga> righe = new ArrayList<>();
        for (OreAgg agg : map.values()) {
            String nomeCompleto = ((agg.utente.getNome() == null ? "" : agg.utente.getNome()) + " "
                    + (agg.utente.getCognome() == null ? "" : agg.utente.getCognome())).trim();
            if (nomeCompleto.isEmpty()) {
                nomeCompleto = agg.utente.getUsername();
            }
            righe.add(OreLavorateReportDto.Riga.builder()
                    .utenteId(agg.utente.getId())
                    .nomeCompleto(nomeCompleto)
                    .oreLavorate(Math.round(agg.ore * 100.0) / 100.0)
                    .giorniAssenzaApprovata(agg.giorniAssenza)
                    .build());
        }

        return OreLavorateReportDto.builder()
                .from(from)
                .to(to)
                .righe(righe)
                .build();
    }

    private static class OreAgg {
        private Utente utente;
        private double ore = 0.0;
        private long giorniAssenza = 0L;
    }
}
