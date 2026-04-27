package com.db.mdm.gestionale.be.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.db.mdm.gestionale.be.entity.Allegato;
import com.db.mdm.gestionale.be.entity.Cantiere;
import com.db.mdm.gestionale.be.entity.Utente;

public interface AllegatoService {
    Allegato saveAllegato(MultipartFile file, Cantiere cantiere, Utente uploader) throws Exception;
    List<Allegato> listByCantiereAndDateRange(Long cantiereId, LocalDateTime from, LocalDateTime to);
    Allegato findById(Long id);
    void softDelete(Long id);
    byte[] downloadFile(String storagePath) throws Exception;
    void deleteFileFromStorage(String storagePath) throws Exception;
}
