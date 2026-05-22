package com.suporte.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recebimento de dados de webhook para criação de tickets
 * 
 * Representa o payload JSON recebido via webhook.site ou outra origem.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketWebhookRequestDTO {

    @NotBlank(message = "O cliente é obrigatório")
    private String cliente;

    private String telefone;

    /** Nome da pessoa atendida (Contato WhatsApp), quando disponível. */
    private String nomeContato;

    @NotBlank(message = "A mensagem é obrigatória")
    private String mensagem;

    private String canal;

    private String conexao;

    private Integer contatoSolicitanteId;

    private String prioridade;

    /** Quando informado, usa este Cliente contratante em vez de buscarOuCriarCliente (Sprint 191). */
    private Integer clienteContratanteId;

    /** WhatsApp matriz da entrada (Sprint 191). */
    private Integer whatsappMatrizId;

}
