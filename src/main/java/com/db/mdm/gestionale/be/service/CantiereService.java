package com.db.mdm.gestionale.be.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.db.mdm.gestionale.be.entity.Allegato;
import com.db.mdm.gestionale.be.entity.Cantiere;

public interface CantiereService {
    Cantiere createCantiereWithOptionalFile(Cantiere cantiere, MultipartFile file) throws Exception;
    Cantiere updateCantiereWithOptionalFile(Long id, Cantiere payload, MultipartFile file, boolean removeFile) throws Exception;
    List<Cantiere> findAll(boolean includeDeleted);
    Optional<Cantiere> findOptionalById(Long id);
    void softDelete(Long id);
    void restore(Long id);
    List<Allegato> listAllegatiForCantiere(Long cantiereId, LocalDateTime from, LocalDateTime to);
    Allegato findAllegatoByIdAndCantiere(Long allegatoId, Long cantiereId);
    byte[] downloadFileFromStorage(String storagePath) throws Exception;
}
