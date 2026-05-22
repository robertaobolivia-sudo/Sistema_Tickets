package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSlaBlocoDTO {

    private long dentroDoPrazo;
    private long proximoVencimento;
    private long vencido;
    private long pausado;
    private long cumprido;
    private long violado;
    private long naoCalculado;
}
