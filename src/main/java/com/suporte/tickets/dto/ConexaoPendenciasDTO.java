package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConexaoPendenciasDTO {

    private String conexao;
    private Integer quantidadePendencias;
    private List<TicketPendenciaConexaoDTO> tickets;
}
