package com.suporte.tickets.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa uma carteira de atendimento
 * 
 * Uma carteira agrupa clientes relacionados para fins de organização
 * e gestão de tickets de suporte.
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
