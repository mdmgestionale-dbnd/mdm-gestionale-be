package com.db.mdm.gestionale.be.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.db.mdm.gestionale.be.entity.Notifica;
import com.db.mdm.gestionale.be.entity.Veicolo;
import com.db.mdm.gestionale.be.repository.NotificaRepository;
import com.db.mdm.gestionale.be.repository.VeicoloRepository;
import com.db.mdm.gestionale.be.service.NotificaService;
import com.db.mdm.gestionale.be.service.WebSocketService;
import com.db.mdm.gestionale.be.utils.Constants;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificaServiceImpl implements NotificaService {

    private final NotificaRepository notificaRepository;
    private final VeicoloRepository veicoloRepository;
    private final WebSocketService webSocketService;

    @Override
    @Transactional(readOnly = true)
    public List<Notifica> findAll(boolean soloNonLette) {
        if (soloNonLette) {
            return notificaRepository.findActionable(LocalDate.now().plusDays(30));
        }
        return notificaRepository.findByIsDeletedFalseOrderByCreatedAtDesc();
    }

    @Override
    @Transactional
    public Notifica markAsRead(Long id) {
        Notifica notifica = notificaRepository.findById(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Notifica non trovata"));
        notifica.setLetta(true);
        Notifica saved = notificaRepository.save(notifica);
        broadcastNotificaChange("mark-read", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        List<Notifica> all = notificaRepository.findByIsDeletedFalseAndLettaFalseOrderByCreatedAtDesc();
        if (all.isEmpty()) {
            return;
        }
        all.forEach(n -> n.setLetta(true));
        notificaRepository.saveAll(all);
        broadcastNotificaChange("mark-all-read", null);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Notifica notifica = notificaRepository.findById(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Notifica non trovata"));
        notifica.setDeleted(true);
        notificaRepository.save(notifica);
        broadcastNotificaChange("delete", id);
    }

    @Override
    @Transactional
    public int generaNotificheScadenzeVeicoli(int giorniPreavviso) {
        LocalDate oggi = LocalDate.now();
        LocalDate limite = oggi.plusDays(Math.max(0, giorniPreavviso));
        int generated = 0;

        List<Veicolo> veicoli = veicoloRepository.findByIsDeletedFalseOrderByTargaAsc();
        List<Notifica> nuove = new ArrayList<>();

        for (Veicolo v : veicoli) {
            generated += maybeCreateVehicleNotification(
                    nuove, v, "SCADENZA_ASSICURAZIONE", "Assicurazione",
                    v.getScadenzaAssicurazione(), oggi, limite);
            generated += maybeCreateVehicleNotification(
                    nuove, v, "SCADENZA_REVISIONE", "Revisione",
                    v.getScadenzaRevisione(), oggi, limite);
            generated += maybeCreateVehicleNotification(
                    nuove, v, "SCADENZA_BOLLO", "Bollo",
                    v.getScadenzaBollo(), oggi, limite);
        }

        if (!nuove.isEmpty()) {
            notificaRepository.saveAll(nuove);
        }
        if (generated > 0) {
            broadcastNotificaChange("new", null);
        }

        return generated;
    }

    @Scheduled(cron = "0 5 6 * * *")
    @Transactional
    public void scheduledVehicleDeadlines() {
        generaNotificheScadenzeVeicoli(30);
    }

    private int maybeCreateVehicleNotification(
            List<Notifica> nuove,
            Veicolo veicolo,
            String tipo,
            String label,
            LocalDate scadenza,
            LocalDate oggi,
            LocalDate limite) {

        if (scadenza == null || scadenza.isAfter(limite)) {
            return 0;
        }

        String chiave = "VEICOLO:" + tipo + ":" + veicolo.getId() + ":" + scadenza;
        var existing = notificaRepository.findByChiaveUnica(chiave);
        if (existing.isPresent()) {
            Notifica saved = existing.get();
            if (saved.isDeleted()) {
                saved.setDeleted(false);
                saved.setLetta(false);
                saved.setLivello(scadenza.isBefore(oggi) ? "ERROR" : "WARN");
                saved.setMessaggio(buildVehicleDeadlineMessage(label, veicolo.getTarga(), scadenza, oggi));
                notificaRepository.save(saved);
                return 1;
            }
            return 0;
        }

        Notifica notifica = new Notifica();
        notifica.setTipo(tipo);
        notifica.setTitolo(label + " in scadenza - " + veicolo.getTarga());
        notifica.setMessaggio(buildVehicleDeadlineMessage(label, veicolo.getTarga(), scadenza, oggi));
        notifica.setLivello(scadenza.isBefore(oggi) ? "ERROR" : "WARN");
        notifica.setRiferimentoTipo("VEICOLO");
        notifica.setRiferimentoId(veicolo.getId());
        notifica.setDataScadenza(scadenza);
        notifica.setChiaveUnica(chiave);
        nuove.add(notifica);
        return 1;
    }

    private String buildVehicleDeadlineMessage(String label, String targa, LocalDate scadenza, LocalDate oggi) {
        long delta = scadenza.toEpochDay() - oggi.toEpochDay();
        if (delta < 0) {
            return label + " del veicolo " + targa + " scaduta il " + scadenza + ".";
        }
        if (Objects.equals(delta, 0L)) {
            return label + " del veicolo " + targa + " in scadenza oggi (" + scadenza + ").";
        }
        return label + " del veicolo " + targa + " in scadenza tra " + delta + " giorni (" + scadenza + ").";
    }

    private void broadcastNotificaChange(String action, Long id) {
        String payload = "{\"entity\":\"notifica\",\"action\":\"" + action + "\",\"id\":" + (id == null ? "null" : id) + "}";
        webSocketService.broadcast(Constants.MSG_NOTIFICATION, payload);
    }
}
