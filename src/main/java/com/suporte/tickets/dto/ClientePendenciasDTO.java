package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Pendências operacionais agrupadas por Cliente contratante (Dashboard). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientePendenciasDTO {

    private String cliente;
    private Integer quantidadePendencias;
    private List<TicketPendenciaClienteDTO> tickets;
}
