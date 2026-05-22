package com.suporte.tickets.dto;

import com.suporte.tickets.entity.TicketInteracaoTipo;
import com.suporte.tickets.entity.TicketInteracaoVisibilidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketInteracaoRequestDTO {

    @NotNull(message = "Tipo de interacao e obrigatorio")
    private TicketInteracaoTipo tipoInteracao;

    @NotNull(message = "Visibilidade e obrigatoria")
    private TicketInteracaoVisibilidade visibilidade;

    @NotBlank(message = "Mensagem e obrigatoria")
    private String mensagem;
}
