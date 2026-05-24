package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para resposta de tickets
 * 
 * Retorna os dados completos do ticket em formato JSON
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDTO {

    private Long id;
    private String numeroTicket;
    private Long analistaResponsavelId;
    private String analistaResponsavelNome;
    private String analistaResponsavelEmail;
    private String cliente;
    /** Id do cliente vinculado (Chats / arte do header). */
    private Integer clienteId;
    /** URL pública da arte do header Chats no cadastro do cliente (Sprint 180/181). */
    private String clienteArteHeaderChatsUrl;
    private String telefone;
    private String telefoneContato;
    private String email;
    private String empresa;
    private String cnpj;
    private String cidade;
    private String uf;
    private Integer contatoId;
    private String contatoNome;
    private String contatoWhatsapp;
    private String contatoEmail;
    private String contatoEmpresaLocal;
    private String contatoCidade;
    private String contatoUf;
    private String contatoObservacoes;
    /** Telefone de origem do atendimento (entrada WhatsApp), Sprint 292. */
    private String atendimentoTelefone;
    private String atendimentoTelefoneTipo;
    /** WhatsApp matriz da entrada, Sprint 191. */
    private Integer whatsappMatrizId;
    private String whatsappMatrizNumero;
    private String whatsappMatrizNome;
    /** Sprint F16: RECEPTIVO_WHATSAPP | ATIVO_MANUAL (null em histórico sem preenchimento). */
    private String origemTicket;
    private String canal;
    private String mensagemInicial;
    private String status;
    private String prioridade;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataPrimeiroAtendimento;
    private LocalDateTime slaPrimeiroAtendimentoVencimento;
    private Boolean slaPrimeiroAtendimentoCumprido;
    private LocalDateTime slaPrimeiroAtendimentoCalculadoEm;
    private String slaPrimeiroAtendimentoStatus;
    private LocalDateTime slaResolucaoVencimento;
    private Boolean slaResolucaoCumprido;
    private LocalDateTime slaResolucaoCalculadoEm;
    private String slaResolucaoStatus;
    private Boolean slaPausado;
    private LocalDateTime slaPausaInicio;
    private Long slaResolucaoMinutosPausados;
    private LocalDateTime dataEncerramento;
    private Integer tmeMinutosUteis;
    private Integer tmaMinutosUteis;
    private Long grupoCategoriaId;
    private String grupoCategoriaNome;
    private Long subgrupoCategoriaId;
    private String subgrupoCategoriaNome;
    private Long motivoId;
    private String motivoNome;
    private String comentarioEncerramento;
    /** Sprint 274 — classificação operacional (indevido). */
    private String classificacaoOperacional;
    private LocalDateTime classificadoOperacionalEm;
    private Long classificadoOperacionalPorAnalistaId;
    private String comentarioClassificacaoOperacional;
    private String observacaoAtendimento;
    private Boolean escalonado;
    private LocalDateTime escalonadoEm;
    private String escalonamentoObservacao;
    private Long escalonadoPorAnalistaId;
    private String escalonadoPorNome;

    private String satisfacaoStatus;
    private Integer satisfacaoNota;
    private String satisfacaoComentario;
    private String satisfacaoEnvioStatus;
    private LocalDateTime satisfacaoEnviadaEm;
    private LocalDateTime satisfacaoRespondidaEm;
    private LocalDateTime satisfacaoExpiraEm;
    /** Link público da pesquisa (retornado no encerramento com pesquisa, Sprint 212). */
    private String avaliacaoLinkPublico;

    /** Sprint F5: metadados apenas em GET /api/tickets/ativo. */
    private String consultaAtivoModo;
    private Boolean consultaAtivoLegadoDeprecated;
    private String consultaAtivoLegadoMotivo;
}
