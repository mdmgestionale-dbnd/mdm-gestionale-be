package com.db.mdm.gestionale.be.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssegnazionePianificazioneRequestDto {

    @NotNull
    private Long cantiereId;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    private String note;

    private List<Long> membroIds;
    private List<Long> veicoloIds;
    private List<MaterialeUsatoDto> materialiUsati;

    @Data
    public static class MaterialeUsatoDto {
        @NotNull
        private Long inventarioId;
        @NotNull
        private BigDecimal quantita;
        private String descrizione;
    }
}
