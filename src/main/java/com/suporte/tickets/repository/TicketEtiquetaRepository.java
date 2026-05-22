package com.suporte.tickets.repository;

import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketEtiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketEtiquetaRepository extends JpaRepository<TicketEtiqueta, Long> {

    List<TicketEtiqueta> findByTicketOrderByEtiqueta_NomeAsc(Ticket ticket);

    void deleteByTicket(Ticket ticket);
}
