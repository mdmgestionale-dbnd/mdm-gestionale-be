package com.db.mdm.gestionale.be.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final ReportService reportService;

    @GetMapping("/ore-lavorate")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public OreLavorateReportDto oreLavorate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long utenteId) {
        return reportService.getOreLavorate(from, to, utenteId);
    }

    @GetMapping("/ore-lavorate.xlsx")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE')")
    public ResponseEntity<byte[]> oreLavorateExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        byte[] file = reportService.exportOreLavorateExcel(from, to);
        String filename = "report-ore-" + from.format(FILE_DATE) + "_" + to.format(FILE_DATE) + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}
