package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOperacaoClienteB2BCardDTO {

    private Integer clienteId;
    private String clienteNome;
    private List<DashboardOperacaoChamadoB2BDTO> chamados = new ArrayList<>();
}
