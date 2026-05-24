package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Ticket vinculado ao contato (histórico em Clientes → Contatos, Sprint 262).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContatoTicketHistoricoItemDTO {

    private String protocolo;
    private LocalDateTime dataAbertura;
    private String categoria;
    private String subcategoria;
    private String motivo;
    private String status;
    private LocalDateTime dataEncerramento;
    private String satisfacaoStatus;
    private Integer satisfacaoNota;
    /** Sprint 292 — numero usado na abertura/entrada WhatsApp. */
    private String atendimentoTelefone;
    /** PRINCIPAL ou ADICIONAL. */
    private String atendimentoTelefoneTipo;
}
