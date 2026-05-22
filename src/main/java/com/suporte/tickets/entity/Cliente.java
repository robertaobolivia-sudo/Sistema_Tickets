package com.suporte.tickets.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Contratante F5 (conexão/carteira da operação).
 *
 * <p>Modelo alvo (Sprint 188+): cadastro do cliente da F5 — dono da conexão, arte White Label
 * do Chats e futuros WhatsApps matriz. A pessoa final atendida via WhatsApp será a entidade
 * {@code Contato} (sprint futura).</p>
 *
 * <p>Legado: FK opcional {@link #carteira} e telefones no cadastro ainda usados por tickets/Chats
 * até a Fase 1 completa.</p>
 */
@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O nome do cliente é obrigatório")
    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String telefone;

    @Column(name = "telefone_contato", length = 20)
    private String telefoneContato;

    @Column(length = 150)
    private String email;

    @Column(length = 150)
    private String empresa;

    @Column(length = 18)
    private String cnpj;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(length = 255)
    private String endereco;

    @Column(length = 30)
    private String status = "ATIVO";

    @Enumerated(EnumType.STRING)
    @Column(name = "classificacao_cliente", length = 30)
    private ClassificacaoCliente classificacaoCliente = ClassificacaoCliente.SEM_CLASSIFICACAO;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carteira_id", nullable = true)
    private Carteira carteira;

    /** URL pública da arte do header Chats (Sprint 180). */
    @Column(name = "arte_header_chats_url", length = 512)
    private String arteHeaderChatsUrl;

    @PrePersist
    void prePersist() {
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }
        sincronizarStatusEAtivo();
        normalizarClassificacao();
        dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        sincronizarStatusEAtivo();
        normalizarClassificacao();
        dataAtualizacao = LocalDateTime.now();
    }

    private void normalizarClassificacao() {
        classificacaoCliente = ClassificacaoCliente.effective(classificacaoCliente);
    }

    private void sincronizarStatusEAtivo() {
        if (status == null || status.isBlank()) {
            status = Boolean.FALSE.equals(ativo) ? "INATIVO" : "ATIVO";
        }
        ativo = !"INATIVO".equalsIgnoreCase(status);
        if (ativo) {
            status = "ATIVO";
        }
    }

}
