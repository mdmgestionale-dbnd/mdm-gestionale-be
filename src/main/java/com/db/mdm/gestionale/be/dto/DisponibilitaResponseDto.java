package com.db.mdm.gestionale.be.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DisponibilitaResponseDto {
    private boolean disponibili;
    private List<Long> membriOccupati;
    private List<Long> veicoliOccupati;
}
