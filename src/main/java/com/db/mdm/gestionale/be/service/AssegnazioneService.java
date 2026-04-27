package com.db.mdm.gestionale.be.service;

import java.time.LocalDateTime;
import java.util.List;

import com.db.mdm.gestionale.be.dto.AssegnazioneCalendarioDto;
import com.db.mdm.gestionale.be.dto.AssegnazionePianificazioneRequestDto;
import com.db.mdm.gestionale.be.dto.DisponibilitaRequestDto;
import com.db.mdm.gestionale.be.dto.DisponibilitaResponseDto;
import com.db.mdm.gestionale.be.entity.Allegato;
import com.db.mdm.gestionale.be.entity.Assegnazione;
import org.springframework.web.multipart.MultipartFile;

public interface AssegnazioneService {
    Assegnazione createPianificazione(AssegnazionePianificazioneRequestDto request);
    Assegnazione updatePianificazione(Long id, AssegnazionePianificazioneRequestDto request);
    List<AssegnazioneCalendarioDto> findCalendario(LocalDateTime from, LocalDateTime to);
    List<AssegnazioneCalendarioDto> findDeleted();
    DisponibilitaResponseDto checkDisponibilita(DisponibilitaRequestDto request);
    Assegnazione findById(Long id);
    void softDelete(Long id);
    void restore(Long id);
    Allegato uploadAllegato(Long id, MultipartFile file) throws Exception;
    void addMateriale(Long id, AssegnazionePianificazioneRequestDto.MaterialeUsatoDto materiale);
}
