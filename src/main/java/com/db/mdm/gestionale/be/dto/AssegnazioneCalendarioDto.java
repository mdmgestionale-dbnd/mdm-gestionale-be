package com.db.mdm.gestionale.be.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssegnazioneCalendarioDto {
    private Long id;
    private Long cantiereId;
    private String cantiereNome;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String note;
    private List<Long> membroIds;
    private List<String> membroNomi;
    private List<Long> veicoloIds;
    private List<String> veicoloNomi;
    private List<MaterialeDto> materiali;

    @Data
    @Builder
    public static class MaterialeDto {
        private Long id;
        private Long inventarioId;
        private Long magazzinoId;
        private String magazzinoNome;
        private String articoloNome;
        private java.math.BigDecimal quantita;
        private String descrizione;
    }
}
