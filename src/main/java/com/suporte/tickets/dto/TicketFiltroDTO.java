package com.suporte.tickets.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class TicketFiltroDTO {

    private String numeroTicket;
    /** Busca parcial por nome (legado). Preferir {@link #clienteId}. */
    private String cliente;
    private Integer clienteId;
    private String status;
    private String prioridade;
    private Long analistaId;
    private String canal;
    private String grupo;
    private String subgrupo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataFim;

    private String textoLivre;

    private String slaPrimeiroAtendimentoStatus;

    private String slaResolucaoStatus;

    private Boolean escalonado;

    private Long motivoId;

    /** TicketSatisfacaoStatus (status_envio). */
    private String statusPesquisa;

    private Integer notaAvaliacao;

    /** TicketSatisfacaoEnvioStatus. */
    private String envioStatus;

    /** Sprint F18: {@link com.suporte.tickets.entity.TicketOrigem} persistido em origem_ticket. */
    private String origemTicket;
}
