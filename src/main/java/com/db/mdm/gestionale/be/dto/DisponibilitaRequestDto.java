package com.db.mdm.gestionale.be.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class DisponibilitaRequestDto {
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private List<Long> membroIds;
    private List<Long> veicoloIds;
    private Long excludeAssegnazioneId;
}
