package com.suporte.tickets.repository;

import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de banco de dados da entidade Ticket
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer>, JpaSpecificationExecutor<Ticket> {

    /**
     * Busca um ticket pelo número único
     * @param numeroTicket Número do ticket no formato TK-XXXXXX
     * @return Optional com o ticket, ou vazio se não encontrado
     */
    Optional<Ticket> findByNumeroTicket(String numeroTicket);

    Optional<Ticket> findFirstByCliente_IdAndStatusInOrderByDataAberturaDesc(
            Integer clienteId,
            List<TicketStatus> statuses);

    Optional<Ticket> findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
            Integer clienteId,
            Integer contatoId,
            List<TicketStatus> statuses);

    long countByContato_Id(Integer contatoId);

    long countByContato_IdAndStatusIn(Integer contatoId, List<TicketStatus> statuses);

    @Query("""
            SELECT t FROM Ticket t
            LEFT JOIN FETCH t.grupoCategoria
            LEFT JOIN FETCH t.subgrupoCategoria
            LEFT JOIN FETCH t.motivo
            WHERE t.contato.id = :contatoId
            ORDER BY t.dataAbertura DESC
            """)
    List<Ticket> findHistoricoByContatoIdOrderByDataAberturaDesc(@Param("contatoId") Integer contatoId);

    @Query("""
            SELECT t FROM Ticket t
            WHERE t.cliente.id = :clienteId
              AND t.contato.id = :contatoId
              AND t.status IN :statuses
            ORDER BY COALESCE(t.dataEncerramento, t.dataAbertura) DESC
            """)
    List<Ticket> findUltimoEncerradoPorClienteEContato(
            @Param("clienteId") Integer clienteId,
            @Param("contatoId") Integer contatoId,
            @Param("statuses") List<TicketStatus> statuses,
            Pageable pageable);

    List<Ticket> findByCliente_IdInAndStatusInOrderByDataAberturaDesc(
            List<Integer> clienteIds,
            List<TicketStatus> statuses,
            Pageable pageable);

    long countByCliente_Id(Integer clienteId);

    @Query("""
            SELECT t FROM Ticket t
            LEFT JOIN FETCH t.grupoCategoria
            WHERE t.cliente.id = :clienteId
              AND t.numeroTicket <> :excludeNumero
            ORDER BY t.dataAbertura DESC
            """)
    List<Ticket> findRecentesByClienteExcluindoNumero(
            @Param("clienteId") Integer clienteId,
            @Param("excludeNumero") String excludeNumero,
            Pageable pageable);

    @Query("""
            SELECT t FROM Ticket t
            LEFT JOIN FETCH t.grupoCategoria
            WHERE t.cliente.id = :clienteId
              AND t.status IN :statuses
              AND t.numeroTicket <> :excludeNumero
            ORDER BY t.dataEncerramento DESC, t.dataAbertura DESC
            """)
    List<Ticket> findEncerradosByClienteExcluindoNumero(
            @Param("clienteId") Integer clienteId,
            @Param("statuses") List<TicketStatus> statuses,
            @Param("excludeNumero") String excludeNumero,
            Pageable pageable);

    /**
     * Retorna todos os tickets ordenados por data de abertura (decrescente)
     * @return Lista de tickets ordenada
     */
    List<Ticket> findAllByOrderByDataAberturaDesc();

    /**
     * Retorna todos os tickets com um status específico, ordenados por data de abertura (decrescente)
     * @param status Status do ticket
     * @return Lista de tickets com o status especificado
     */
    List<Ticket> findByStatusOrderByDataAberturaDesc(TicketStatus status);

    List<Ticket> findByAnalistaResponsavelIdOrderByDataAberturaAsc(Long analistaId);

    List<Ticket> findByAnalistaResponsavelIdAndStatusOrderByDataAberturaAsc(Long analistaId, TicketStatus status);

    List<Ticket> findByStatusInOrderByDataAberturaAsc(List<TicketStatus> statuses);

    long countByAnalistaResponsavelId(Long analistaResponsavelId);

    long countByStatus(TicketStatus status);

    long countByAnalistaResponsavelIsNull();

    long countByAnalistaResponsavelIsNullAndStatusIn(List<TicketStatus> statuses);

    long countByDataAberturaGreaterThanEqual(LocalDateTime dataInicio);

    long countByStatusAndDataAberturaGreaterThanEqual(TicketStatus status, LocalDateTime dataInicio);

    long countByStatusAndDataEncerramentoGreaterThanEqual(TicketStatus status, LocalDateTime dataInicio);

    List<Ticket> findByDataPrimeiroAtendimentoIsNotNullAndDataAberturaIsNotNull();

    List<Ticket> findByDataEncerramentoIsNotNullAndDataAberturaIsNotNull();

    boolean existsByMensagemInicial(String mensagemInicial);

    /**
     * Obtém o próximo número sequencial para tickets
     * @return O ID do último ticket + 1 ou 1 se não houver tickets
     */
    @Query("SELECT COALESCE(MAX(t.id), 0) + 1 FROM Ticket t")
    Integer getNextSequence();

    @Query("SELECT COALESCE(MAX(t.id), 0) FROM Ticket t")
    Integer findMaxId();

    @Query("SELECT MAX(t.dataAbertura) FROM Ticket t")
    Optional<LocalDateTime> findMaxDataAbertura();

    List<Ticket> findByIdGreaterThanOrderByIdAsc(Integer id, Pageable pageable);

    List<Ticket> findByDataAberturaGreaterThanOrderByDataAberturaAsc(LocalDateTime dataAbertura, Pageable pageable);

    @Query("""
            SELECT DISTINCT t FROM Ticket t
            LEFT JOIN FETCH t.cliente
            LEFT JOIN FETCH t.analistaResponsavel
            LEFT JOIN FETCH t.grupoCategoria
            LEFT JOIN FETCH t.subgrupoCategoria
            WHERE (:inicio IS NULL OR t.dataAbertura >= :inicio)
              AND (:fim IS NULL OR t.dataAbertura <= :fim)
            ORDER BY t.dataAbertura DESC
            """)
    List<Ticket> findByDataAberturaPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    @Query("""
            SELECT t FROM Ticket t
            JOIN FETCH t.motivo m
            JOIN FETCH m.subgrupoCategoria sub
            JOIN FETCH sub.grupoCategoria gr
            LEFT JOIN FETCH t.cliente c
            WHERE t.dataEncerramento IS NOT NULL
              AND t.status <> :statusIndevido
              AND (:inicio IS NULL OR t.dataEncerramento >= :inicio)
              AND (:fim IS NULL OR t.dataEncerramento <= :fim)
              AND (:clienteId IS NULL OR c.id = :clienteId)
              AND (:motivoId IS NULL OR m.id = :motivoId)
            """)
    List<Ticket> findEncerradosComMotivoParaIndicadores(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("clienteId") Integer clienteId,
            @Param("motivoId") Long motivoId,
            @Param("statusIndevido") TicketStatus statusIndevido);

    @Query("""
            SELECT t FROM Ticket t
            LEFT JOIN FETCH t.motivo m
            LEFT JOIN FETCH m.subgrupoCategoria
            LEFT JOIN FETCH t.subgrupoCategoria
            WHERE t.dataEncerramento IS NOT NULL
              AND t.dataEncerramento >= :inicio
              AND t.dataEncerramento < :fimExclusivo
              AND t.status <> :statusIndevido
            """)
    List<Ticket> findEncerradosOperacionaisNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fimExclusivo") LocalDateTime fimExclusivo,
            @Param("statusIndevido") TicketStatus statusIndevido);

    @Query("""
            SELECT t FROM Ticket t
            JOIN FETCH t.cliente c
            LEFT JOIN FETCH t.analistaResponsavel
            LEFT JOIN FETCH t.contato
            WHERE c.ativo = true
              AND t.status IN :statuses
            ORDER BY c.nome ASC, t.dataAbertura ASC
            """)
    List<Ticket> findOperacionaisPorClientesAtivos(@Param("statuses") List<TicketStatus> statuses);

}
