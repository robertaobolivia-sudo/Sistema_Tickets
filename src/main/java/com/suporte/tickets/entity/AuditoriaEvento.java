package com.suporte.tickets.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "analista_id")
    private Long analistaId;

    @Column(name = "analista_nome", length = 150)
    private String analistaNome;

    @Column(name = "perfil_acesso", length = 20)
    private String perfilAcesso;

    @Column(nullable = false, length = 80)
    private String acao;

    @Column(length = 50)
    private String entidade;

    @Column(name = "entidade_id", length = 100)
    private String entidadeId;

    @Column(length = 500)
    private String descricao;

    @Column(name = "ip_origem", length = 64)
    private String ipOrigem;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @PrePersist
    void prePersist() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now();
        }
    }
}
