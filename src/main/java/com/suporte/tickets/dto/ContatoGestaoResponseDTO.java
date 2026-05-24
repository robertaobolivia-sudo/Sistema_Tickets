package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contato para telas Clientes → Contatos e resumo no cadastro (Sprint 254).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContatoGestaoResponseDTO {

    private Integer id;
    private Integer clienteId;
    private String clienteRazaoSocial;
    private String nome;
    private String whatsapp;
    private String email;
    private String empresaLocal;
    private String cidade;
    private String uf;
    private Boolean ativo;
    /** Nomes de etiquetas ativas, separados por vírgula (máx. exibição). */
    private String etiquetasResumo;
    /** True se o contato possui Indevido, Contato Pessoal ou Propaganda (Sprint 271). */
    private Boolean temEtiquetaOperacional;
    /** Total de chamados vinculados ao contato. */
    private Integer totalChamados;
    /** Chamados em status considerados ativos. */
    private Integer chamadosAtivos;
}
