package com.db.mdm.gestionale.be.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OreLavorateReportDto {
    private LocalDate from;
    private LocalDate to;
    private List<Riga> righe;

    @Data
    @Builder
    public static class Riga {
        private Long utenteId;
        private String nomeCompleto;
        private double oreLavorate;
        private long giorniAssenzaApprovata;
    }
}
