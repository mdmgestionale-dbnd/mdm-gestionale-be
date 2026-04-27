package com.db.mdm.gestionale.be.service;

import java.util.List;
import com.db.mdm.gestionale.be.entity.Cliente;

public interface ClienteService {
    Cliente save(Cliente entity);
    List<Cliente> findAll(boolean includeDeleted);
    Cliente findById(Long id);
    void softDelete(Long id);
    void restore(Long id);
}
