package com.db.mdm.gestionale.be.service;

import com.db.mdm.gestionale.be.dto.DashboardOverviewDto;

public interface DashboardService {
    DashboardOverviewDto getOverview(int giorniScadenzeVeicoli);
}
