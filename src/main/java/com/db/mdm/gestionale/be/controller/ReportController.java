package com.db.mdm.gestionale.be.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.db.mdm.gestionale.be.dto.OreLavorateReportDto;
import com.db.mdm.gestionale.be.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/ore-lavorate")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public OreLavorateReportDto oreLavorate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long utenteId) {
        return reportService.getOreLavorate(from, to, utenteId);
    }
}
