package com.db.mdm.gestionale.be.service;

import java.time.LocalDate;

import com.db.mdm.gestionale.be.dto.OreLavorateReportDto;

public interface ReportService {
    OreLavorateReportDto getOreLavorate(LocalDate from, LocalDate to, Long utenteId);
    byte[] exportOreLavorateExcel(LocalDate from, LocalDate to);
}
