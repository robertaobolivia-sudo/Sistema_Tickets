package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOperacaoAgoraDTO {

    private DashboardOperacaoCardDTO emAtendimento;
    private DashboardOperacaoCardDTO emFila;
}
