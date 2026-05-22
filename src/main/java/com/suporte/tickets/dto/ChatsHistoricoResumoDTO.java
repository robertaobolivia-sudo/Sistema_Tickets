package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatsHistoricoResumoDTO {

    private long totalTicketsCliente;
    private ChatsHistoricoTicketResumoDTO ultimoTicketEncerrado;
    private List<ChatsHistoricoTicketResumoDTO> ticketsRecentes = new ArrayList<>();
}
