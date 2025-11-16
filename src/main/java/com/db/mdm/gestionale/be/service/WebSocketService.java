package com.db.mdm.gestionale.be.service;

public interface WebSocketService {
    void broadcast(String tipoEvento, String payload);
}