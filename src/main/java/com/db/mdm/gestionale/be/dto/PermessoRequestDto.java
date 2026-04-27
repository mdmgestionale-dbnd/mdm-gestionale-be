package com.db.mdm.gestionale.be.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermessoRequestDto {
    @NotNull
    private String tipo;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    private String note;
    private Long allegatoId;
}
