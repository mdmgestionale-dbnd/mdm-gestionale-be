package com.db.mdm.gestionale.be.service;

import java.util.List;

import com.db.mdm.gestionale.be.entity.Notifica;

public interface NotificaService {
    List<Notifica> findAll(boolean soloNonLette);
    Notifica markAsRead(Long id);
    void markAllAsRead();
    void softDelete(Long id);
    int generaNotificheScadenzeVeicoli(int giorniPreavviso);
}
