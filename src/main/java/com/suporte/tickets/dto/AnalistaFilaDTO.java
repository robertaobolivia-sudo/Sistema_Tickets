package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalistaFilaDTO {

    private Long id;
    private String nome;
    private String nomeCompleto;
    private String email;
    private String nivel;
    private String perfilAcesso;
    private String fotoUrl;
    private String statusOperador;
    private Boolean online;
    private Integer quantidadeTickets;
    private List<TicketResponseDTO> tickets;
}
