package com.suporte.tickets.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cadastro global Conexões/Revendas (Configurações). Não vincula Cliente contratante (F40).
 */
@Entity
@Table(name = "carteiras")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carteira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O nome da carteira é obrigatório")
    @Column(nullable = false, length = 100)
    private String nome;

    /** URL pública da arte horizontal do header Chats (white label). */
    @Column(name = "arte_header_chats_url", length = 255)
    private String arteHeaderChatsUrl;

}
