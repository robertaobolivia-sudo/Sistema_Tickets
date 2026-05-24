package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOperacaoClienteB2BDTO {

    private List<DashboardOperacaoClienteB2BCardDTO> clientes = new ArrayList<>();
}
