package com.db.mdm.gestionale.be.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardOverviewDto {
    private long clientiAttivi;
    private long cantieriAttivi;
    private long veicoliAttivi;
    private long assegnazioniTotali;
    private long assegnazioniOggi;
    private long notificheNonLette;
    private List<AssegnazioneCalendarioDto> prossimeAssegnazioni;
    private List<VeicoloScadenzaDto> veicoliInScadenza;

    @Data
    @Builder
    public static class VeicoloScadenzaDto {
        private Long id;
        private String targa;
        private String marca;
        private String modello;
        private String tipoScadenza;
        private String dataScadenza;
    }
}
