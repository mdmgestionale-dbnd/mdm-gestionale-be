package com.db.mdm.gestionale.be.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.db.mdm.gestionale.be.dto.OreLavorateReportDto;
import com.db.mdm.gestionale.be.entity.AssegnazioneMembro;
import com.db.mdm.gestionale.be.entity.Permesso;
import com.db.mdm.gestionale.be.entity.Utente;
import com.db.mdm.gestionale.be.repository.AssegnazioneMembroRepository;
import com.db.mdm.gestionale.be.repository.PermessoRepository;
import com.db.mdm.gestionale.be.repository.UtenteRepository;
import com.db.mdm.gestionale.be.service.ReportService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final AssegnazioneMembroRepository assegnazioneMembroRepository;
    private final PermessoRepository permessoRepository;
    private final UtenteRepository utenteRepository;

    @Override
    @Transactional(readOnly = true)
    public OreLavorateReportDto getOreLavorate(LocalDate from, LocalDate to, Long utenteId) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Intervallo data obbligatorio");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("La data finale non puo essere precedente alla data iniziale");
        }

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime endExclusive = to.plusDays(1).atStartOfDay();

        Map<Long, OreAgg> map = new LinkedHashMap<>();
        for (AssegnazioneMembro am : assegnazioneMembroRepository.findAllOverlapping(start, endExclusive)) {
            Utente u = am.getUtente();
            if (utenteId != null && !utenteId.equals(u.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(u.getIsDeleted())) {
                continue;
            }

            LocalDateTime assignmentStart = am.getAssegnazione().getStartAt();
            LocalDateTime assignmentEnd = am.getAssegnazione().getEndAt();
            LocalDateTime effectiveStart = assignmentStart.isBefore(start) ? start : assignmentStart;
            LocalDateTime effectiveEnd = assignmentEnd.isAfter(endExclusive) ? endExclusive : assignmentEnd;
            if (!effectiveEnd.isAfter(effectiveStart)) {
                continue;
            }

            double hours = Duration.between(effectiveStart, effectiveEnd).toMinutes() / 60.0;

            OreAgg agg = map.computeIfAbsent(u.getId(), x -> new OreAgg());
            agg.utente = u;
            agg.ore += hours;
        }

        if (utenteId != null && !map.containsKey(utenteId)) {
            utenteRepository.findById(utenteId).ifPresent(u -> {
                if (!Boolean.TRUE.equals(u.getIsDeleted())) {
                    OreAgg agg = new OreAgg();
                    agg.utente = u;
                    map.put(utenteId, agg);
                }
            });
        }

        for (Map.Entry<Long, OreAgg> entry : map.entrySet()) {
            Long userId = entry.getKey();
            List<Permesso> approved = permessoRepository
                    .findByUtenteIdAndStatoAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(
                            userId, "APPROVATO", to, from);
            long assenze = 0;
            for (Permesso p : approved) {
                LocalDate d1 = p.getStartDate().isBefore(from) ? from : p.getStartDate();
                LocalDate d2 = p.getEndDate().isAfter(to) ? to : p.getEndDate();
                assenze += Duration.between(d1.atStartOfDay(), d2.plusDays(1).atStartOfDay()).toDays();
            }
            entry.getValue().giorniAssenza = assenze;
        }

        List<OreLavorateReportDto.Riga> righe = new ArrayList<>();
        for (OreAgg agg : map.values()) {
            String nomeCompleto = ((agg.utente.getNome() == null ? "" : agg.utente.getNome()) + " "
                    + (agg.utente.getCognome() == null ? "" : agg.utente.getCognome())).trim();
            if (nomeCompleto.isEmpty()) {
                nomeCompleto = agg.utente.getUsername();
            }
            righe.add(OreLavorateReportDto.Riga.builder()
                    .utenteId(agg.utente.getId())
                    .nomeCompleto(nomeCompleto)
                    .oreLavorate(Math.round(agg.ore * 100.0) / 100.0)
                    .giorniAssenzaApprovata(agg.giorniAssenza)
                    .build());
        }

        return OreLavorateReportDto.builder()
                .from(from)
                .to(to)
                .righe(righe)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportOreLavorateExcel(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Intervallo data obbligatorio");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("La data finale non puo essere precedente alla data iniziale");
        }

        List<LocalDate> days = from.datesUntil(to.plusDays(1)).toList();
        List<Utente> users = utenteRepository.findByLivelloInAndIsDeletedFalse(List.of(0, 1, 2));
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime endExclusive = to.plusDays(1).atStartOfDay();

        Map<Long, UserGrid> grids = new LinkedHashMap<>();
        users.forEach(u -> grids.put(u.getId(), new UserGrid(u)));

        for (AssegnazioneMembro am : assegnazioneMembroRepository.findAllOverlapping(start, endExclusive)) {
            UserGrid grid = grids.get(am.getUtente().getId());
            if (grid == null) {
                continue;
            }
            LocalDateTime effectiveStart = max(am.getAssegnazione().getStartAt(), start);
            LocalDateTime effectiveEnd = min(am.getAssegnazione().getEndAt(), endExclusive);
            LocalDate cursor = effectiveStart.toLocalDate();
            while (!cursor.isAfter(effectiveEnd.minusNanos(1).toLocalDate())) {
                LocalDateTime dayStart = cursor.atStartOfDay();
                LocalDateTime dayEnd = cursor.plusDays(1).atStartOfDay();
                LocalDateTime sliceStart = max(effectiveStart, dayStart);
                LocalDateTime sliceEnd = min(effectiveEnd, dayEnd);
                if (sliceEnd.isAfter(sliceStart)) {
                    double hours = Duration.between(sliceStart, sliceEnd).toMinutes() / 60.0;
                    String cantiere = am.getAssegnazione().getCantiere().getNome();
                    grid.addHours(cantiere, cursor, hours);
                }
                cursor = cursor.plusDays(1);
            }
        }

        for (UserGrid grid : grids.values()) {
            for (Permesso p : permessoRepository.findByUtenteIdAndStatoAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(
                    grid.user.getId(), "APPROVATO", to, from)) {
                LocalDate leaveStart = p.getStartDate().isBefore(from) ? from : p.getStartDate();
                LocalDate leaveEnd = p.getEndDate().isAfter(to) ? to : p.getEndDate();
                for (LocalDate d = leaveStart; !d.isAfter(leaveEnd); d = d.plusDays(1)) {
                    if ("MALATTIA".equals(p.getTipo())) {
                        grid.malattiaDays++;
                    } else {
                        grid.ferieDays++;
                    }
                }
            }
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setAlignment(HorizontalAlignment.CENTER);

            var summary = workbook.createSheet("Riepilogo");
            Row h = summary.createRow(0);
            set(h, 0, "Dipendente", headerStyle);
            set(h, 1, "Ore lavorate", headerStyle);
            set(h, 2, "Giorni ferie", headerStyle);
            set(h, 3, "Giorni malattia", headerStyle);

            int r = 1;
            Set<String> usedSheetNames = new HashSet<>();
            usedSheetNames.add("Riepilogo");
            for (UserGrid grid : grids.values()) {
                Row row = summary.createRow(r++);
                set(row, 0, displayName(grid.user), null);
                numeric(row, 1, round(grid.totalHours()), numberStyle);
                numeric(row, 2, grid.ferieDays, numberStyle);
                numeric(row, 3, grid.malattiaDays, numberStyle);
                createUserSheet(workbook, grid, days, headerStyle, numberStyle, usedSheetNames);
            }
            for (int i = 0; i < 4; i++) summary.autoSizeColumn(i);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Errore generazione Excel", e);
        }
    }

    private void createUserSheet(XSSFWorkbook workbook, UserGrid grid, List<LocalDate> days, CellStyle headerStyle, CellStyle numberStyle, Set<String> usedSheetNames) {
        String safeName = displayName(grid.user).replaceAll("[\\\\/?*\\[\\]:]", " ").trim();
        String baseName = safeName.isBlank() ? "Dipendente " + grid.user.getId() : safeName;
        String sheetName = uniqueSheetName(baseName, usedSheetNames);
        var sheet = workbook.createSheet(sheetName);
        Row header = sheet.createRow(0);
        set(header, 0, "Cantiere", headerStyle);
        for (int i = 0; i < days.size(); i++) {
            set(header, i + 1, days.get(i).toString().substring(5), headerStyle);
        }
        set(header, days.size() + 1, "Totale", headerStyle);

        int rowIndex = 1;
        for (Map.Entry<String, Map<LocalDate, Double>> cantiere : grid.hoursByCantiere.entrySet()) {
            Row row = sheet.createRow(rowIndex++);
            set(row, 0, cantiere.getKey(), null);
            double total = 0;
            for (int i = 0; i < days.size(); i++) {
                double hours = cantiere.getValue().getOrDefault(days.get(i), 0.0);
                if (hours > 0) {
                    numeric(row, i + 1, round(hours), numberStyle);
                }
                total += hours;
            }
            numeric(row, days.size() + 1, round(total), numberStyle);
        }

        int leaveStart = Math.max(rowIndex + 1, 2);
        Row ferie = sheet.createRow(leaveStart);
        set(ferie, 0, "Totale giorni ferie", headerStyle);
        numeric(ferie, 1, grid.ferieDays, numberStyle);
        Row malattia = sheet.createRow(leaveStart + 1);
        set(malattia, 0, "Totale giorni malattia", headerStyle);
        numeric(malattia, 1, grid.malattiaDays, numberStyle);

        sheet.autoSizeColumn(0);
        for (int i = 1; i <= days.size() + 1; i++) {
            sheet.setColumnWidth(i, 2800);
        }
        sheet.createFreezePane(1, 1);
    }

    private static void set(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
    }

    private static void numeric(Row row, int column, double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
    }

    private static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String displayName(Utente utente) {
        String nomeCompleto = ((utente.getNome() == null ? "" : utente.getNome()) + " "
                + (utente.getCognome() == null ? "" : utente.getCognome())).trim();
        return nomeCompleto.isEmpty() ? utente.getUsername() : nomeCompleto;
    }

    private String uniqueSheetName(String rawName, Set<String> usedSheetNames) {
        String base = rawName.substring(0, Math.min(31, rawName.length())).trim();
        if (base.isBlank()) {
            base = "Dipendente";
        }
        String candidate = base;
        int index = 2;
        while (usedSheetNames.contains(candidate)) {
            String suffix = " " + index++;
            int maxBase = Math.max(1, 31 - suffix.length());
            candidate = base.substring(0, Math.min(maxBase, base.length())).trim() + suffix;
        }
        usedSheetNames.add(candidate);
        return candidate;
    }

    private static class OreAgg {
        private Utente utente;
        private double ore = 0.0;
        private long giorniAssenza = 0L;
    }

    private static class UserGrid {
        private final Utente user;
        private final Map<String, Map<LocalDate, Double>> hoursByCantiere = new TreeMap<>();
        private long ferieDays = 0L;
        private long malattiaDays = 0L;

        private UserGrid(Utente user) {
            this.user = user;
        }

        private void addHours(String cantiere, LocalDate day, double hours) {
            hoursByCantiere.computeIfAbsent(cantiere, k -> new TreeMap<>())
                    .merge(day, hours, Double::sum);
        }

        private double totalHours() {
            return hoursByCantiere.values().stream()
                    .flatMap(m -> m.values().stream())
                    .mapToDouble(Double::doubleValue)
                    .sum();
        }
    }
}
