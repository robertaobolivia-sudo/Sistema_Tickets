package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoSlaVerificacaoResultadoDTO {

    private long verificados;
    private long notificacoesCriadas;
    private long ignoradasDuplicadas;
}
