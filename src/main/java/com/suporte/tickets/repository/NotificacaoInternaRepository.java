package com.suporte.tickets.repository;

import com.suporte.tickets.entity.NotificacaoInterna;
import com.suporte.tickets.entity.NotificacaoTipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoInternaRepository extends JpaRepository<NotificacaoInterna, Long> {

    boolean existsByTipoAndTicketNumeroAndLidaFalse(NotificacaoTipo tipo, String ticketNumero);

    List<NotificacaoInterna> findTop50ByOrderByCriadoEmDesc();

    List<NotificacaoInterna> findTop50ByLidaFalseOrderByCriadoEmDesc();

    long countByLidaFalse();

    List<NotificacaoInterna> findByLidaFalseOrderByCriadoEmDesc();
}
