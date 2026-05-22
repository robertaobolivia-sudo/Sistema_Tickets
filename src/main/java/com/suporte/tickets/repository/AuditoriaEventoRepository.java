package com.suporte.tickets.repository;

import com.suporte.tickets.entity.AuditoriaEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface AuditoriaEventoRepository extends JpaRepository<AuditoriaEvento, Long>,
        JpaSpecificationExecutor<AuditoriaEvento> {

    long countByDataHoraBefore(LocalDateTime dataHora);

    @Modifying
    @Transactional
    long deleteByDataHoraBefore(LocalDateTime dataHora);
}
