package com.suporte.tickets.repository;

import com.suporte.tickets.entity.PrioridadeTicket;
import com.suporte.tickets.entity.SlaMeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SlaMetaRepository extends JpaRepository<SlaMeta, Long> {

    List<SlaMeta> findAllByOrderByPrioridadeAsc();

    List<SlaMeta> findByAtivoTrueOrderByPrioridadeAsc();

    Optional<SlaMeta> findFirstByPrioridadeAndAtivoTrue(PrioridadeTicket prioridade);

    Optional<SlaMeta> findByPrioridade(PrioridadeTicket prioridade);

    boolean existsByPrioridadeAndAtivoTrueAndIdNot(PrioridadeTicket prioridade, Long id);
}
