package com.suporte.tickets.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa um ticket de suporte no sistema
 * 
 * Um ticket registra uma solicitação de suporte técnico remoto,
 * armazenando todas as informações relevantes ao atendimento.
 */
@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O número do ticket é obrigatório")
    @Column(nullable = false, length = 20, unique = true)
    private String numeroTicket;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /** Pessoa final WhatsApp (Sprint 189); opcional ate migracao completa. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contato_id")
    private Contato contato;

    /** Numero matriz que recebeu a mensagem (Sprint 191). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "whatsapp_matriz_id")
    private WhatsappMatriz whatsappMatriz;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "analista_responsavel_id")
    private Analista analistaResponsavel;

    @Column(length = 50)
    private String canal;

    @Lob
    private String mensagemInicial;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TicketStatus status = TicketStatus.ABERTO;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade", length = 20)
    private PrioridadeTicket prioridade;

    /** Sprint F16: origem operacional (receptivo WhatsApp vs abertura manual). */
    @Enumerated(EnumType.STRING)
    @Column(name = "origem_ticket", length = 30)
    private TicketOrigem origemTicket;

    @Column(nullable = false)
    private LocalDateTime dataAbertura;

    @Column(nullable = true)
    private LocalDateTime dataPrimeiroAtendimento;

    @Column(name = "sla_primeiro_atendimento_vencimento", nullable = true)
    private LocalDateTime slaPrimeiroAtendimentoVencimento;

    @Column(name = "sla_primeiro_atendimento_cumprido", nullable = true)
    private Boolean slaPrimeiroAtendimentoCumprido;

    @Column(name = "sla_primeiro_atendimento_calculado_em", nullable = true)
    private LocalDateTime slaPrimeiroAtendimentoCalculadoEm;

    @Column(name = "sla_resolucao_vencimento", nullable = true)
    private LocalDateTime slaResolucaoVencimento;

    @Column(name = "sla_resolucao_cumprido", nullable = true)
    private Boolean slaResolucaoCumprido;

    @Column(name = "sla_resolucao_calculado_em", nullable = true)
    private LocalDateTime slaResolucaoCalculadoEm;

    @Column(name = "sla_pausado", nullable = true)
    private Boolean slaPausado;

    @Column(name = "sla_pausa_inicio", nullable = true)
    private LocalDateTime slaPausaInicio;

    @Column(name = "sla_resolucao_minutos_pausados", nullable = true)
    private Long slaResolucaoMinutosPausados;

    @Column(nullable = true)
    private LocalDateTime dataEncerramento;

    @Column(nullable = true)
    private Integer tmeMinutosUteis;

    @Column(nullable = true)
    private Integer tmaMinutosUteis;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "grupo_categoria_id")
    private GrupoCategoria grupoCategoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subgrupo_categoria_id")
    private SubgrupoCategoria subgrupoCategoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "motivo_id")
    private Motivo motivo;

    @Lob
    private String comentarioEncerramento;

    @Column(name = "escalonado", nullable = true)
    private Boolean escalonado;

    @Column(name = "escalonado_em", nullable = true)
    private LocalDateTime escalonadoEm;

    @Lob
    @Column(name = "escalonamento_observacao", nullable = true)
    private String escalonamentoObservacao;

    /** Observação interna do atendimento (painel Chats); distinta do comentário de encerramento. */
    @Lob
    @Column(name = "observacao_atendimento", nullable = true)
    private String observacaoAtendimento;

    @Column(name = "escalonado_por_analista_id", nullable = true)
    private Long escalonadoPorAnalistaId;

    /** Classificação operacional (Sprint 274), preenchida ao status INDEVIDO. */
    @Enumerated(EnumType.STRING)
    @Column(name = "classificacao_operacional", length = 30)
    private TicketClassificacaoOperacional classificacaoOperacional;

    @Column(name = "classificado_operacional_em")
    private LocalDateTime classificadoOperacionalEm;

    @Column(name = "classificado_operacional_por_analista_id")
    private Long classificadoOperacionalPorAnalistaId;

    @Lob
    @Column(name = "comentario_classificacao_operacional")
    private String comentarioClassificacaoOperacional;

    /** Telefone pelo qual o atendimento entrou (WhatsApp), Sprint 292. */
    @Column(name = "atendimento_telefone", length = 30)
    private String atendimentoTelefone;

    @Column(name = "atendimento_telefone_normalizado", length = 20)
    private String atendimentoTelefoneNormalizado;

    /** PRINCIPAL ou ADICIONAL em relacao ao Contato. */
    @Column(name = "atendimento_telefone_tipo", length = 15)
    private String atendimentoTelefoneTipo;

}
