package com.suporte.tickets.repository;

import com.suporte.tickets.entity.TicketInteracao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketInteracaoRepository extends JpaRepository<TicketInteracao, Long> {

    List<TicketInteracao> findByTicketNumeroTicketOrderByCriadoEmAsc(String numeroTicket);
}
