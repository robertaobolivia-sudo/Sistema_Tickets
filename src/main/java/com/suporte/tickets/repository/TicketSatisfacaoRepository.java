package com.suporte.tickets.repository;

import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.entity.TicketSatisfacaoStatus;
import com.suporte.tickets.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TicketSatisfacaoRepository extends JpaRepository<TicketSatisfacao, Long> {

    Optional<TicketSatisfacao> findByTicket_Id(Integer ticketId);

    List<TicketSatisfacao> findByTicket_IdIn(Collection<Integer> ticketIds);

    boolean existsByTicket_Id(Integer ticketId);

    List<TicketSatisfacao> findByStatusAndExpiraEmBefore(
            TicketSatisfacaoStatus status, LocalDateTime expiraEm);

    Optional<TicketSatisfacao> findByTokenRespostaHash(String tokenRespostaHash);

    @Query("""
            SELECT s FROM TicketSatisfacao s
            JOIN FETCH s.ticket t
            LEFT JOIN FETCH t.cliente c
            WHERE (:inicio IS NULL OR s.criadoEm >= :inicio)
              AND (:fim IS NULL OR s.criadoEm <= :fim)
              AND (:nota IS NULL OR s.nota = :nota)
              AND (:status IS NULL OR t.status = :status)
              AND (:clienteId IS NULL OR c.id = :clienteId)
              AND (:termoCliente IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :termoCliente, '%')))
            ORDER BY s.criadoEm DESC
            """)
    List<TicketSatisfacao> findByFiltros(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("nota") Integer nota,
            @Param("status") TicketStatus status,
            @Param("termoCliente") String termoCliente,
            @Param("clienteId") Integer clienteId);

    @Query("""
            SELECT s FROM TicketSatisfacao s
            JOIN FETCH s.ticket t
            LEFT JOIN FETCH t.cliente c
            LEFT JOIN FETCH t.motivo m
            WHERE (:inicio IS NULL OR s.criadoEm >= :inicio)
              AND (:fim IS NULL OR s.criadoEm <= :fim)
              AND (:clienteId IS NULL OR c.id = :clienteId)
              AND (:motivoId IS NULL OR m.id = :motivoId)
              AND (:statusPesquisa IS NULL OR s.status = :statusPesquisa)
              AND (:nota IS NULL OR s.nota = :nota)
              AND t.status <> :statusIndevido
            """)
    List<TicketSatisfacao> findForIndicadoresEncerramento(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("clienteId") Integer clienteId,
            @Param("motivoId") Long motivoId,
            @Param("statusPesquisa") TicketSatisfacaoStatus statusPesquisa,
            @Param("nota") Integer nota,
            @Param("statusIndevido") TicketStatus statusIndevido);

    /** Sprint 265 — contato com pesquisa respondida nota &lt;= 2 (avaliação ruim). */
    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
            FROM TicketSatisfacao s
            JOIN s.ticket t
            WHERE t.contato.id = :contatoId
              AND s.nota IS NOT NULL
              AND s.nota <= 2
            """)
    boolean existsAvaliacaoRuimPorContatoId(@Param("contatoId") Integer contatoId);

    @Query("""
            SELECT s FROM TicketSatisfacao s
            JOIN FETCH s.ticket t
            WHERE t.status <> :statusIndevido
            """)
    List<TicketSatisfacao> findAllOperacionalExcluindoIndevido(
            @Param("statusIndevido") TicketStatus statusIndevido);
}
