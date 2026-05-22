package com.suporte.tickets.repository;

import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketAnexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketAnexoRepository extends JpaRepository<TicketAnexo, Long> {

    List<TicketAnexo> findByTicketOrderByCriadoEmDesc(Ticket ticket);

    Optional<TicketAnexo> findByIdAndTicket(Long id, Ticket ticket);
}
