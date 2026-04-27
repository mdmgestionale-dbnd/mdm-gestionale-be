package com.db.mdm.gestionale.be.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.db.mdm.gestionale.be.dto.AssegnazioneCalendarioDto;
import com.db.mdm.gestionale.be.dto.DashboardOverviewDto;
import com.db.mdm.gestionale.be.entity.Assegnazione;
import com.db.mdm.gestionale.be.entity.Veicolo;
import com.db.mdm.gestionale.be.repository.AssegnazioneMembroRepository;
import com.db.mdm.gestionale.be.repository.AssegnazioneRepository;
import com.db.mdm.gestionale.be.repository.AssegnazioneVeicoloRepository;
import com.db.mdm.gestionale.be.repository.CantiereRepository;
import com.db.mdm.gestionale.be.repository.ClienteRepository;
import com.db.mdm.gestionale.be.repository.NotificaRepository;
import com.db.mdm.gestionale.be.repository.VeicoloRepository;
import com.db.mdm.gestionale.be.service.DashboardService;
import com.db.mdm.gestionale.be.service.NotificaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ClienteRepository clienteRepository;
    private final CantiereRepository cantiereRepository;
    private final VeicoloRepository veicoloRepository;
    private final AssegnazioneRepository assegnazioneRepository;
    private final NotificaRepository notificaRepository;
    private final AssegnazioneMembroRepository assegnazioneMembroRepository;
    private final AssegnazioneVeicoloRepository assegnazioneVeicoloRepository;
    private final NotificaService notificaService;

    @Override
    @Transactional
    public DashboardOverviewDto getOverview(int giorniScadenzeVeicoli) {
        notificaService.generaNotificheScadenzeVeicoli(giorniScadenzeVeicoli);
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<Assegnazione> upcoming = assegnazioneRepository
                .findTop10ByIsDeletedFalseAndEndAtAfterOrderByStartAtAsc(LocalDateTime.now());

        List<AssegnazioneCalendarioDto> prossimeAssegnazioni = new ArrayList<>();
        for (Assegnazione a : upcoming) {
            prossimeAssegnazioni.add(AssegnazioneCalendarioDto.builder()
                    .id(a.getId())
                    .cantiereId(a.getCantiere().getId())
                    .cantiereNome(a.getCantiere().getNome())
                    .startAt(a.getStartAt())
                    .endAt(a.getEndAt())
                    .note(a.getNote())
                    .membroIds(assegnazioneMembroRepository.findByAssegnazioneId(a.getId()).stream()
                            .map(x -> x.getUtente().getId()).toList())
                    .veicoloIds(assegnazioneVeicoloRepository.findByAssegnazioneId(a.getId()).stream()
                            .map(x -> x.getVeicolo().getId()).toList())
                    .build());
        }

        List<DashboardOverviewDto.VeicoloScadenzaDto> veicoliInScadenza = buildVeicoliScadenza(giorniScadenzeVeicoli);

        return DashboardOverviewDto.builder()
                .clientiAttivi(clienteRepository.countByIsDeletedFalse())
                .cantieriAttivi(cantiereRepository.countByIsDeletedFalse())
                .veicoliAttivi(veicoloRepository.countByIsDeletedFalse())
                .assegnazioniTotali(assegnazioneRepository.countByIsDeletedFalse())
                .assegnazioniOggi(assegnazioneRepository.countByIsDeletedFalseAndStartAtBetween(start, end))
                .notificheNonLette(notificaRepository.countByIsDeletedFalseAndLettaFalse())
                .prossimeAssegnazioni(prossimeAssegnazioni)
                .veicoliInScadenza(veicoliInScadenza)
                .build();
    }

    private List<DashboardOverviewDto.VeicoloScadenzaDto> buildVeicoliScadenza(int giorni) {
        LocalDate limit = LocalDate.now().plusDays(Math.max(0, giorni));
        List<Veicolo> veicoli = veicoloRepository.findExpiringByLimit(limit);
        List<DashboardOverviewDto.VeicoloScadenzaDto> out = new ArrayList<>();

        for (Veicolo v : veicoli) {
            if (v.getScadenzaAssicurazione() != null && !v.getScadenzaAssicurazione().isAfter(limit)) {
                out.add(toScadenzaDto(v, "ASSICURAZIONE", v.getScadenzaAssicurazione().toString()));
            }
            if (v.getScadenzaRevisione() != null && !v.getScadenzaRevisione().isAfter(limit)) {
                out.add(toScadenzaDto(v, "REVISIONE", v.getScadenzaRevisione().toString()));
            }
            if (v.getScadenzaBollo() != null && !v.getScadenzaBollo().isAfter(limit)) {
                out.add(toScadenzaDto(v, "BOLLO", v.getScadenzaBollo().toString()));
            }
        }

        return out;
    }

    private DashboardOverviewDto.VeicoloScadenzaDto toScadenzaDto(Veicolo v, String tipo, String data) {
        return DashboardOverviewDto.VeicoloScadenzaDto.builder()
                .id(v.getId())
                .targa(v.getTarga())
                .marca(v.getMarca())
                .modello(v.getModello())
                .tipoScadenza(tipo)
                .dataScadenza(data)
                .build();
    }
}
