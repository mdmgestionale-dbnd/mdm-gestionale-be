package com.db.mdm.gestionale.be.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.db.mdm.gestionale.be.entity.Notifica;
import com.db.mdm.gestionale.be.entity.Utente;
import com.db.mdm.gestionale.be.entity.Veicolo;
import com.db.mdm.gestionale.be.repository.NotificaRepository;
import com.db.mdm.gestionale.be.repository.VeicoloRepository;
import com.db.mdm.gestionale.be.service.NotificaService;
import com.db.mdm.gestionale.be.service.UtenteService;
import com.db.mdm.gestionale.be.service.WebSocketService;
import com.db.mdm.gestionale.be.utils.Constants;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificaServiceImpl implements NotificaService {

    private final NotificaRepository notificaRepository;
    private final VeicoloRepository veicoloRepository;
    private final UtenteService utenteService;
    private final WebSocketService webSocketService;

    @Override
    @Transactional(readOnly = true)
    public List<Notifica> findAll(boolean soloNonLette) {
        Utente current = utenteService.getCurrentUtenteOrNull();
        if (current != null && !Integer.valueOf(0).equals(current.getLivello())) {
            if (soloNonLette) {
                return notificaRepository.findActionableForUser(current.getId(), LocalDate.now().plusDays(30));
            }
            return notificaRepository.findByIsDeletedFalseAndDestinatarioIdOrderByCreatedAtDesc(current.getId());
        }
        if (soloNonLette) {
            return notificaRepository.findActionable(LocalDate.now().plusDays(30))
                    .stream()
                    .filter(n -> n.getDestinatarioId() == null)
                    .toList();
        }
        return notificaRepository.findByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .filter(n -> n.getDestinatarioId() == null)
                .toList();
    }

    @Override
    @Transactional
    public Notifica markAsRead(Long id) {
        Notifica notifica = notificaRepository.findById(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Notifica non trovata"));
        ensureCanMutate(notifica);
        notifica.setLetta(true);
        Notifica saved = notificaRepository.save(notifica);
        broadcastNotificaChange("mark-read", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        Utente current = utenteService.getCurrentUtenteOrNull();
        List<Notifica> all = current != null && !Integer.valueOf(0).equals(current.getLivello())
                ? notificaRepository.findByIsDeletedFalseAndLettaFalseAndDestinatarioIdOrderByCreatedAtDesc(current.getId())
                : notificaRepository.findByIsDeletedFalseAndLettaFalseOrderByCreatedAtDesc();
        if (current == null || Integer.valueOf(0).equals(current.getLivello())) {
            all = all.stream().filter(n -> n.getDestinatarioId() == null).toList();
        }
        all = all.stream()
                .filter(n -> n.getDataScadenza() == null && !n.getTipo().startsWith("SCADENZA_"))
                .toList();
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
        ensureCanMutate(notifica);
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
        closeStaleVehicleNotifications(veicoli, limite);
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

    private void closeStaleVehicleNotifications(List<Veicolo> veicoli, LocalDate limite) {
        var veicoliById = veicoli.stream().collect(java.util.stream.Collectors.toMap(Veicolo::getId, v -> v));
        List<Notifica> stale = notificaRepository.findByRiferimentoTipoAndIsDeletedFalse("VEICOLO")
                .stream()
                .filter(n -> isVehicleNotificationStale(n, veicoliById.get(n.getRiferimentoId()), limite))
                .toList();
        if (stale.isEmpty()) {
            return;
        }
        stale.forEach(n -> {
            n.setLetta(true);
            n.setDeleted(true);
        });
        notificaRepository.saveAll(stale);
        broadcastNotificaChange("vehicle-stale-closed", null);
    }

    private boolean isVehicleNotificationStale(Notifica notifica, Veicolo veicolo, LocalDate limite) {
        if (veicolo == null || veicolo.isDeleted()) {
            return true;
        }
        LocalDate currentDeadline = switch (notifica.getTipo()) {
            case "SCADENZA_ASSICURAZIONE" -> veicolo.getScadenzaAssicurazione();
            case "SCADENZA_REVISIONE" -> veicolo.getScadenzaRevisione();
            case "SCADENZA_BOLLO" -> veicolo.getScadenzaBollo();
            default -> null;
        };
        return currentDeadline == null
                || currentDeadline.isAfter(limite)
                || !currentDeadline.equals(notifica.getDataScadenza());
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
                saved.setMessaggio(buildVehicleDeadlineMessage(label, veicolo.getTarga(), scadenza));
                notificaRepository.save(saved);
                return 1;
            }
            return 0;
        }

        Notifica notifica = new Notifica();
        notifica.setTipo(tipo);
        notifica.setTitolo(label + " in scadenza - " + veicolo.getTarga());
        notifica.setMessaggio(buildVehicleDeadlineMessage(label, veicolo.getTarga(), scadenza));
        notifica.setLivello(scadenza.isBefore(oggi) ? "ERROR" : "WARN");
        notifica.setRiferimentoTipo("VEICOLO");
        notifica.setRiferimentoId(veicolo.getId());
        notifica.setDataScadenza(scadenza);
        notifica.setChiaveUnica(chiave);
        nuove.add(notifica);
        return 1;
    }

    private String buildVehicleDeadlineMessage(String label, String targa, LocalDate scadenza) {
        return label + " del veicolo " + targa + " con scadenza il " + scadenza + ".";
    }

    private void ensureCanMutate(Notifica notifica) {
        Utente current = utenteService.getCurrentUtenteOrNull();
        if (current == null) {
            return;
        }
        if (Integer.valueOf(0).equals(current.getLivello()) && notifica.getDestinatarioId() == null) {
            return;
        }
        if (!current.getId().equals(notifica.getDestinatarioId())) {
            throw new AccessDeniedException("Notifica non disponibile per l'utente corrente");
        }
    }

    private void broadcastNotificaChange(String action, Long id) {
        String payload = "{\"entity\":\"notifica\",\"action\":\"" + action + "\",\"id\":" + (id == null ? "null" : id) + "}";
        webSocketService.broadcast(Constants.MSG_NOTIFICATION, payload);
    }
}
