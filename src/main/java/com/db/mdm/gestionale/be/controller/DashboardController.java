package com.db.mdm.gestionale.be.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.db.mdm.gestionale.be.dto.DashboardOverviewDto;
import com.db.mdm.gestionale.be.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISORE','DIPENDENTE')")
    public DashboardOverviewDto overview(@RequestParam(defaultValue = "30") int giorniScadenzeVeicoli) {
        return dashboardService.getOverview(giorniScadenzeVeicoli);
    }
}
